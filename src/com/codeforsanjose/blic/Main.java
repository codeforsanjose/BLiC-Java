package com.codeforsanjose.blic;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Main {

    static Map<URL, WebPage> pages;
    static ThreadPoolExecutor executor;
    static int depth_limit = 3;
    static int max_thread_limit = 10;
    static int threads_running_count = 0;
    static int fail_tolerance = 3;
    static URL base_url;

    public static void main(String[] args) {
        // System.out.println("Broken Link Checker");
        if (args.length < 1) {
            System.out.println("Please provide the URL of a website to be checked for broken links.");
            System.out.println("The URL would be the first argument passed into the program when run in the command line.");
            System.out.println("Example usage:");
            System.out.println("java -jar blic.jar http://codeforsanjose.com/");    // FIXME: export this project to a jar
            return;
        }
        String arg_url = args[0];
        System.out.println("BLiC : "+arg_url);
        System.out.println("Please wait. This may take some time.");

        try {
            base_url = new URL(arg_url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return;
        }

        pages = new ConcurrentHashMap<URL, WebPage>();
        pages.put(base_url, new WebPage(null, base_url));

        //printSiteStatuses();

        //executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(max_thread_limit);
        executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();


        // populate the initial list of links to crawl
        WebPage w = getNextWebPage();

        try {
            executor.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        boolean running = true;
        while (running) {
            w = getNextWebPage();
            //System.out.println("\r"+executor.getActiveCount()+" threads running");
            if (w != null){
                startCrawler(w);
            }
            running = !isDone();
        }



        try {
            executor.shutdown();
            executor.awaitTermination(60, TimeUnit.SECONDS);
            System.out.println("done: " + isDone());
            printSiteStatuses();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private static WebPage getNextWebPage() {
        for (Object o : pages.entrySet()) {
            Map.Entry<String, WebPage> cur = (Map.Entry<String, WebPage>) o;
            WebPage w = cur.getValue();

            if (!w.isLocked() && w.getStatus() == null && w.getDepth() <= depth_limit) {
                w.lock();
                return w;
            } else if (!w.isLocked() && w.getStatus() != null && (w.getFailureCount() < fail_tolerance && (w.getStatus() == -1 || w.getStatus() == 429))){
                w.lock();
                return w;
            }
        }
        return null;
    }

    private static void printWorkerSummary(){

        System.out.println("\r "+executor.getActiveCount());
    }

    private static void startCrawler(WebPage page) {

        Crawler task = new Crawler(++threads_running_count, page, pages);
        // System.out.println("A new crawler has been added : " + task.getName());
        executor.execute(task);
    }

    private static boolean isDone() {
        for (Object o : pages.entrySet()) {
            Map.Entry<String, WebPage> cur = (Map.Entry<String, WebPage>) o;
            WebPage w = cur.getValue();
            if ( w.getStatus() == null) {
                return false;
            }
        }
        if (executor.getActiveCount()==0){
            System.out.println("No threads are currently running");
            return true;
        }
        System.out.println("Found no items in the list that needed work.");
        return true;
    }

    private static void printSiteStatuses() {
        for (Object o : pages.entrySet()) {
            Map.Entry<String, WebPage> cur = (Map.Entry<String, WebPage>) o;
            System.out.println(cur.getValue().toString());
        }
    }

}
