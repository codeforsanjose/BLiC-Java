package com.codeforsanjose.blic;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class WebPage {
    private AtomicBoolean locked;
    private WebPage linkedFromPage;
    private URL url;
    private Integer status;
    private int depth;
    private AtomicInteger failCount;


    private String failReason;


    public WebPage(WebPage parent, URL url) {
        this.linkedFromPage = parent;
        this.url = url;
        this.status = null;
        this.depth = 0;
        this.failCount = new AtomicInteger(0);
        this.locked = new AtomicBoolean(false);
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(String url) {
        try {
            this.url = new URL(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public String getFailReason() {
        return failReason;
    }

    public void setFailReason(String failReason) {
        this.failReason = failReason;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    @Override
    public String toString() {
        String parentUrl = "";
        if (this.linkedFromPage != null) {
            parentUrl = "parent: \"" + this.linkedFromPage.getUrl().toString() + "\", ";
        }
        String status_string = (this.status == null) ? "not yet checked" : this.status.toString();
        return "http_status: " + status_string + ", failure_count: " + this.failCount + ", " + parentUrl + " url:\"" + this.url.toString() + "\"";
    }

    public boolean isLocked() {
        return this.locked.get();
    }
}
