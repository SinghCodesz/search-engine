package com.searchengine.crawler;

import com.searchengine.model.Document;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class MultiThreadedCrawler {
    private final CrawlFrontier frontier;
    private final WebCrawler webCrawler;
    private final int numThreads;
    private final List<Document> crawledDocuments;
    private final Set<String> failedUrls;
    private long startTime;

    public MultiThreadedCrawler(int maxPages, int numThreads) {
        this.frontier = new CrawlFrontier(maxPages);
        this.webCrawler = new WebCrawler();
        this.numThreads = numThreads;
        this.crawledDocuments = Collections.synchronizedList(new ArrayList<>());
        this.failedUrls = Collections.synchronizedSet(new HashSet<>());
    }

    /**
     * Add a seed URL to start crawling from
     */
    public void addSeed(String url) {
        frontier.addSeed(url);
    }

    /**
     * Start crawling with multiple threads
     */
    public List<Document> startCrawling() {
        System.out.println("Starting multi-threaded crawler...");
        System.out.println("Threads: " + numThreads);
        System.out.println("Max pages: " + frontier.getCrawledCount() + " target");

        startTime = System.currentTimeMillis();

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        // Submit worker tasks
        for (int i = 0; i < numThreads; i++) {
            final int threadId = i + 1;
            executor.submit(() -> crawlWorker(threadId));
        }

        // Wait for all threads to finish
        executor.shutdown();
        try {
            // Wait up to 10 minutes
            executor.awaitTermination(10, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            System.err.println("Crawling interrupted");
            executor.shutdownNow();
        }

        long duration = System.currentTimeMillis() - startTime;

        System.out.println("\n========== CRAWL COMPLETE ==========");
        System.out.println("Total pages crawled: " + crawledDocuments.size());
        System.out.println("Failed URLs: " + failedUrls.size());
        System.out.println("Time taken: " + (duration / 1000.0) + " seconds");

        if (!crawledDocuments.isEmpty()) {
            double pagesPerSec = crawledDocuments.size() / (duration / 1000.0);
            System.out.println("Throughput: " + String.format("%.1f", pagesPerSec) + " pages/sec");
        }

        return crawledDocuments;
    }

    /**
     * Worker thread: continuously takes URLs from frontier and crawls them
     */
    private void crawlWorker(int threadId) {
        while (frontier.hasMoreUrls()) {
            String url = frontier.getNextUrl();
            if (url == null) break;

            try {
                // Politeness delay between requests
                Thread.sleep(500);

                // Crawl the page
                Document doc = webCrawler.crawl(url);
                crawledDocuments.add(doc);

                // Add discovered links to frontier
                List<String> newLinks = doc.getOutLinks();
                if (!newLinks.isEmpty()) {
                    frontier.addNewUrls(newLinks);
                }

                // Progress update every 5 pages
                int count = frontier.getCrawledCount();
                if (count % 5 == 0) {
                    System.out.println("[Thread " + threadId + "] Progress: " +
                            count + " crawled, " + frontier.getQueueSize() + " queued");
                }

            } catch (IOException e) {
                failedUrls.add(url);
                System.err.println("[Thread " + threadId + "] Failed: " +
                        url + " - " + e.getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                failedUrls.add(url);
                System.err.println("[Thread " + threadId + "] Error: " +
                        url + " - " + e.getMessage());
            }
        }

        System.out.println("[Thread " + threadId + "] Finished");
    }

    /**
     * Get crawl statistics
     */
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("pagesCrawled", crawledDocuments.size());
        stats.put("failedUrls", failedUrls.size());
        stats.put("queueRemaining", frontier.getQueueSize());
        stats.put("timeElapsed", (System.currentTimeMillis() - startTime) / 1000.0);
        return stats;
    }

    /**
     * Print summary of crawled documents
     */
    public void printSummary() {
        System.out.println("\n========== CRAWLED PAGES ==========");
        List<Document> sorted = new ArrayList<>(crawledDocuments);
        sorted.sort((a, b) -> Integer.compare(b.getOutLinks().size(), a.getOutLinks().size()));

        System.out.println("\nTop 10 pages by outgoing links:");
        sorted.stream().limit(10).forEach(doc -> {
            System.out.println("  " + doc.getTitle());
            System.out.println("    URL: " + doc.getUrl());
            System.out.println("    Links: " + doc.getOutLinks().size());
            System.out.println("    Text: " + doc.getCleanedText().length() + " chars");
            System.out.println();
        });
    }
}