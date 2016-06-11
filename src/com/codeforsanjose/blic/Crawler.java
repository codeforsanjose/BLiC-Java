package com.codeforsanjose.blic;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by falconer_k on 6/10/16.
 */
public class Crawler implements Runnable {
    private WebPage webpage;
    private String name;
    private int id;
    private Map<URL, WebPage> pages;

    public Crawler(int id, WebPage webpage, Map<URL, WebPage> pages) {
        this.id = id;
        this.webpage = webpage;
        this.name = this.webpage.getUrl().toString();
        this.pages = pages;
    }


    public String getName() {
        return this.name;
    }

    @Override
    public void run() {
        Document doc = null;
        if (this.webpage.getFailureCount() > 0) {
            // give this page a rest for a bit
            try {
                TimeUnit.SECONDS.sleep(this.webpage.getFailureCount());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            doc = Jsoup.connect(this.webpage.getUrl().toString()).get();
            if (!this.shouldCrawlPage()) {
                this.webpage.setStatus(200);
                this.webpage.unlock();
                return;
            }
            //System.out.println("\n" + id + ": Crawling " + this.name);
            Elements anchors = doc.select("a");
            ArrayList<URL> unseenLinks = filterUnseen(anchors);
            //System.out.println("\n" + id + ": Found " + unseenLinks.size() + " new links on page");
            for (URL u : unseenLinks) {
                if (u != null && !this.pages.containsKey(u)) {
                    if (Main.depth_limit > this.webpage.getDepth()) {
                        WebPage w = new WebPage(this.webpage, u);
                        w.setDepth(this.webpage.getDepth() + 1);
                        this.pages.put(u, w);
                    }
                }
            }
            this.webpage.setStatus(200);
        } catch (UnsupportedMimeTypeException e) {
            this.webpage.setStatus(200);
        } catch (HttpStatusException e) {
            this.webpage.failureCountIncrement();
            this.webpage.setStatus(e.getStatusCode());
            this.webpage.setFailReason(e.toString());
            System.out.println(e.getMessage());
        } catch (SocketException e) {
            this.webpage.failureCountIncrement();
            this.webpage.setStatus(-1);
            this.webpage.setFailReason(e.toString());
            System.out.println(e.getMessage());
        } catch (IOException e) {
            this.webpage.failureCountIncrement();
            this.webpage.setStatus(-1);
            this.webpage.setFailReason(e.toString());
            System.out.println(e.getMessage());
        }
        System.out.println(webpage.toString());
        this.webpage.unlock();
    }

    private boolean shouldCrawlPage() {
        return this.webpage.getUrl().getHost().equals(Main.base_url.getHost());
    }

    private ArrayList<URL> filterUnseen(Elements anchors) {
        ArrayList<URL> res = new ArrayList<>();
        for (Element a : anchors) {
            URL u = parseUrl(a.attr("abs:href"));
            if (u != null && !pages.containsKey(u)) {
                res.add(u);
            }
        }
        return res;
    }

    private URL parseUrl(String dirtyUrl) {
        String tempUrlString = dirtyUrl;
        URL res = null;
        try {
            if (tempUrlString.startsWith("//")) {
                tempUrlString = "http:" + tempUrlString;
            }
            if (tempUrlString.startsWith("http://") || tempUrlString.startsWith("https://")) {
                res = new URL(tempUrlString);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return res;
    }
}
