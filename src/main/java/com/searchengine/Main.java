package com.searchengine;

import com.searchengine.crawler.MultiThreadedCrawler;
import com.searchengine.model.Document;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        // Create crawler: 50 pages, 5 threads
        MultiThreadedCrawler crawler = new MultiThreadedCrawler(50, 5);

        // Add seed URLs to start crawling
        crawler.addSeed("https://example.com");
        crawler.addSeed("https://httpbin.org");
        crawler.addSeed("https://books.toscrape.com");     // Good: many links
        crawler.addSeed("https://quotes.toscrape.com");    // Good: many links
        // Add more seeds for better coverage

        System.out.println("Starting web crawl...");
        System.out.println("This may take 1-2 minutes depending on network speed.\n");

        // Start crawling
        List<Document> documents = crawler.startCrawling();

        // Print summary
        crawler.printSummary();

        // Check if we got enough pages
        if (documents.size() < 10) {
            System.out.println("\n⚠️  Only crawled " + documents.size() +
                    " pages. This might be due to network issues or restrictive robots.txt.");
            System.out.println("Try adding more seed URLs or using a news website.");
        } else {
            System.out.println("\n✅ Multi-threaded crawler working perfectly!");
        }
    }
}