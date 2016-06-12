package com.codeforsanjose.blic;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CrawlController {

    private Map<URL, WebPage> pages;
    private ThreadPoolExecutor executor;
    private int depth_limit;
    private boolean useFixedThreadPool;
    private int max_thread_limit;
    private int crawler_id;
    private int fail_tolerance;
    private URL base_url;
    private static final Logger log = LogManager.getLogger(CrawlController.class);

    public CrawlController(String base_url, int depth_limit) throws MalformedURLException {
        this.base_url = new URL(base_url);
        this.fail_tolerance = 3;
        this.depth_limit = depth_limit;
        this.useFixedThreadPool = false;
        this.crawler_id = 0;
    }

    public CrawlController(String base_url, int depth_limit, int fail_tolerance) throws MalformedURLException {
        this.base_url = new URL(base_url);
        this.fail_tolerance = fail_tolerance;
        this.depth_limit = depth_limit;
        this.useFixedThreadPool = false;
        this.crawler_id = 0;
    }

    public CrawlController(String base_url, int depth_limit, int fail_tolerance, int max_thread_limit) throws MalformedURLException {
        this.base_url = new URL(base_url);
        this.fail_tolerance = fail_tolerance;
        this.max_thread_limit = max_thread_limit;
        this.useFixedThreadPool = true;
        this.depth_limit = depth_limit;
        this.crawler_id = 0;
    }

    public void crawl() {

        pages = new ConcurrentHashMap<URL, WebPage>();
        pages.put(base_url, new WebPage(null, base_url));

        if (useFixedThreadPool) {
            executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(max_thread_limit);
        } else {
            executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        }

        WebPage w = null;
        boolean running = true;
        while (running) {
            w = getNextWebPage();
            log.trace(executor.getActiveCount() + " threads running");
            if (w != null) {
                startCrawler(w);
            }
            running = !isDone();
        }

        try {
            executor.shutdown();
            executor.awaitTermination(60, TimeUnit.SECONDS);
            log.debug("done: " + isDone());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public List<String> getResults() {
        ArrayList<String> res = new ArrayList<>();
        for (Object o : pages.entrySet()) {
            Map.Entry<String, WebPage> cur = (Map.Entry<String, WebPage>) o;
            res.add(cur.getValue().toString());
        }
        return res;
    }

    private void startCrawler(WebPage page) {
        Crawler task = new Crawler(++crawler_id, page, pages, this.base_url, this.depth_limit);
        log.trace("A new crawler has been added : " + task.toString());
        executor.execute(task);
    }

    /**
     * Iterates over the mapped WebPage instances to find a URL that needs to be tried.
     * Once a candidate is found, it is reserved for processing using a locking mechanism and returns that
     * locked WebPage instance.
     *
     * @return
     */
    private WebPage getNextWebPage() {
        for (Object o : pages.entrySet()) {
            Map.Entry<String, WebPage> cur = (Map.Entry<String, WebPage>) o;
            WebPage w = cur.getValue();

            if (!w.isLocked() && w.getStatus() == null && w.getDepth() <= depth_limit) {
                w.lock();
                return w;
            } else if (!w.isLocked() && w.getStatus() != null && (w.getFailureCount() < fail_tolerance && (w.getStatus() == -1 || w.getStatus() == 429))) {
                w.lock();
                return w;
            }
        }
        return null;
    }

    /**
     * Iterates over the mapped WebPages to see if any URLS have not been checked.
     *
     * @return true if all the URLs have been tried and no threads are running
     */
    private boolean isDone() {
        for (Object o : pages.entrySet()) {
            Map.Entry<String, WebPage> cur = (Map.Entry<String, WebPage>) o;
            WebPage w = cur.getValue();
            if (w.getStatus() == null) {
                return false;
            }
        }
        int t_running = executor.getActiveCount();
        if (t_running != 0) {
            log.trace("No additional items need work, but " + t_running + " threads are still running");
            return false;
        }
        log.debug("Found no more items in the list that needed work.");
        return true;
    }
}
