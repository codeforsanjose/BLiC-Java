package com.codeforsanjose.blic;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class WebPage implements Comparable<WebPage> {
    private AtomicBoolean locked;
    private Map<URL, WebPage> linkedFromPages;
    private URL url;
    private Integer status;
    private int depth;
    private AtomicInteger failCount;
    private ArrayList<String> failReasons;

    public WebPage(WebPage parent, URL url) {
        this.linkedFromPages = new ConcurrentHashMap<>();
        this.url = url;
        this.status = null;
        this.depth = 0;
        this.failReasons = new ArrayList<>();
        this.failCount = new AtomicInteger(0);
        this.locked = new AtomicBoolean(false);

        this.linkedByPageAdd(parent);
    }

    public URL getUrl() {
        return url;
    }

    public void linkedByPageAdd(WebPage page) {
        if (page == null) {
            return;
        }
        this.linkedFromPages.putIfAbsent(page.getUrl(), page);
    }

    public String getLinkedFromPages() {
        List list = new ArrayList();
        for (URL u : this.linkedFromPages.keySet()) {
            list.add("\"" + u.toString() + "\"");
        }
        return "[" + String.join(",", list) + "]";
    }

    public void lock() {
        this.locked.compareAndSet(false, true);
    }

    public void unlock() {
        this.locked.set(false);
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getFailureCount() {
        return failCount.get();
    }

    public void failureCountIncrement() {
        this.failCount.addAndGet(1);
    }

    public String getFailReasons() {
        return "[" + String.join(",", this.failReasons) + "]";
    }

    public void setFailReason(String failReason) {
        this.failReasons.add("\"" + failReason + "\"");
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    @Override
    public String toString() {
        StringJoiner res = new StringJoiner(",");
        res.add("http_status:" + ((this.status == null) ? "not yet checked" : this.status.toString()));
        res.add("url:\"" + this.url.toString()+ "\"");

        if (this.linkedFromPages.size() > 0) {
            res.add("referenced_by:" + this.getLinkedFromPages());
        }
        res.add("failure_count: " + this.failCount);
        if  (this.failReasons.size() > 0) {
            res.add("failure_reasons:" + this.getFailReasons());
        }

        return res.toString();
    }

    public boolean isLocked() {
        return this.locked.get();
    }

    @Override
    public int compareTo(WebPage o) {
        return this.url.toString().compareTo(o.getUrl().toString());
    }
}
