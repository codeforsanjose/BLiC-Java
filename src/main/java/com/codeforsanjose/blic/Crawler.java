package com.codeforsanjose.blic;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Crawler implements Runnable {
    private final URL base;
    private WebPage webpage;
    private String name;
    private int id;
    private int depth_limit;
    private Map<URL, WebPage> pages;
    private static final Logger log = LogManager.getLogger(Crawler.class);

    public Crawler(int id, WebPage webpage, Map<URL, WebPage> pages, URL base, int depth_limit) {
        this.id = id;
        this.base = base;
        this.webpage = webpage;
        this.depth_limit = depth_limit;
        this.name = this.webpage.getUrl().toString();
        this.pages = pages;
    }

    @Override
    public void run() {
        Document doc = null;
        if (this.webpage.getFailureCount() > 0) {
            // give this page a rest for a bit
            try {
                TimeUnit.SECONDS.sleep(this.webpage.getFailureCount());
            } catch (InterruptedException e) {
                log.warn(e);
            }
        }
        try {
            doc = Jsoup.connect(this.webpage.getUrl().toString())
                    //.userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.1")
                    .userAgent("BLiC Broken Link Checker")
                    .referrer("http://www.google.com")
                    .followRedirects(true)
                    .ignoreContentType(true)
                    .get();
            URL actual_url = new URL(doc.location());
            if (urlEquals(base, actual_url) && !urlEquals(this.webpage.getUrl(), actual_url)) {
                this.webpage.setStatus(404);
                this.webpage.setFailReason("Redirected to homepage unexpectedly");
            }
            if (!this.shouldCrawlPage()) {
                this.webpage.setStatus(200);
                this.webpage.unlock();
                return;
            }
            log.info(id + ": Crawling " + this.name);
            Elements anchors = doc.select("a");

            ArrayList<URL> absUrls = mapToAbsolute(anchors);

            log.info(id + ": Found " + absUrls.size() + " links on page");
            for (URL u : absUrls) {
                WebPage p = this.pages.get(u);
                if (p != null && !u.equals(this.webpage.getUrl())) {
                    p.linkedByPageAdd(this.webpage.getUrl());
                }
                if (this.depth_limit > this.webpage.getDepth()) {
                    WebPage w = new WebPage(this.webpage, u);
                    w.setDepth(this.webpage.getDepth() + 1);
                    this.pages.putIfAbsent(u, w);
                }
            }
            this.webpage.setStatus(200);
        } catch (UnsupportedMimeTypeException e) {
            this.webpage.setStatus(200);
        } catch (HttpStatusException e) {
            this.webpage.failureCountIncrement();
            this.webpage.setStatus(e.getStatusCode());
            this.webpage.setFailReason(e.toString());
            log.warn(e);
        } catch (SocketException e) {
            this.webpage.failureCountIncrement();
            this.webpage.setStatus(-1);
            this.webpage.setFailReason(e.toString());
            log.warn(e);
        } catch (IOException e) {
            this.webpage.failureCountIncrement();
            this.webpage.setStatus(-1);
            this.webpage.setFailReason(e.toString());
            log.warn(e);
        }
        log.info(webpage.toString());
        this.webpage.unlock();
    }

    private boolean shouldCrawlPage() {
        return this.webpage.getUrl().getHost().equals(base.getHost());
    }

    private ArrayList<URL> mapToAbsolute(Elements anchors) {
        ArrayList<URL> res = new ArrayList<>();
        for (Element a : anchors) {
            URL u = parseUrl(a.attr("abs:href"));
            if (u != null) {
                res.add(u);
            }
        }
        return res;
    }

    public static boolean urlEquals(URL url1, URL url2) throws MalformedURLException {
        URL url1QueryStripped = getUrlWithoutParameters(url1);
        URL url2QueryStripped = getUrlWithoutParameters(url2);
        return (url1QueryStripped.equals(url2QueryStripped));
    }

    private static URL getUrlWithoutParameters(URL url) throws MalformedURLException {
        // see https://docs.oracle.com/javase/tutorial/networking/urls/urlInfo.html
        return new URL(url.getProtocol(),
                url.getAuthority(),
                url.getFile());

    }

    public static URL parseUrl(String dirtyUrl) {
        String tempUrlString = dirtyUrl;
        URL res = null;
        try {
            if (tempUrlString.startsWith("//")) {
                tempUrlString = "http:" + tempUrlString;
            }
            if (tempUrlString.startsWith("http://") || tempUrlString.startsWith("https://")) {
                res = new URL(tempUrlString.replaceAll("/$", ""));
            }
        } catch (MalformedURLException e) {
            log.warn(e);
        }
        return res;
    }

    public String toString() {
        return this.webpage.toString();
    }
}
