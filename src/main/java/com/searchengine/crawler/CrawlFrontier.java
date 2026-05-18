package com.searchengine.crawler;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CrawlFrontier {
    private final Queue<String> urlQueue;
    private final Set<String> visitedUrls;
    private final int maxPages;
    private int crawledCount;

    public CrawlFrontier(int maxPages) {
        this.urlQueue = new ConcurrentLinkedQueue<>();
        this.visitedUrls = Collections.synchronizedSet(new HashSet<>());
        this.maxPages = maxPages;
        this.crawledCount = 0;
    }

    /**
     * Add seed URLs to start crawling
     */
    public void addSeed(String url) {
        String normalized = normalizeUrl(url);
        if (!visitedUrls.contains(normalized)) {
            urlQueue.add(normalized);
            visitedUrls.add(normalized);
        }
    }

    /**
     * Get next URL to crawl. Returns null if max pages reached.
     */
    public synchronized String getNextUrl() {
        if (crawledCount >= maxPages) {
            return null;
        }
        String url = urlQueue.poll();
        if (url != null) {
            crawledCount++;
        }
        return url;
    }

    /**
     * Add newly discovered URLs from a crawled page
     */
    public void addNewUrls(List<String> urls) {
        for (String url : urls) {
            String normalized = normalizeUrl(url);
            if (!visitedUrls.contains(normalized) && crawledCount + urlQueue.size() < maxPages * 2) {
                visitedUrls.add(normalized);
                urlQueue.add(normalized);
            }
        }
    }

    public boolean hasMoreUrls() {
        return !urlQueue.isEmpty() && crawledCount < maxPages;
    }

    public int getCrawledCount() { return crawledCount; }
    public int getQueueSize() { return urlQueue.size(); }

    /**
     * Normalize URL to avoid duplicates:
     * - Remove trailing slash
     * - Remove www prefix
     * - Lowercase
     */
    private String normalizeUrl(String url) {
        if (url == null) return "";
        url = url.toLowerCase().trim();
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        url = url.replace("https://www.", "https://");
        url = url.replace("http://www.", "http://");
        return url;
    }
}