package com.searchengine;

import com.searchengine.crawler.WebCrawler;
import com.searchengine.model.Document;

public class Main {
    public static void main(String[] args) {
        WebCrawler crawler = new WebCrawler();

        try {
            Document doc = crawler.crawl("https://example.com");

            System.out.println("\n========== CRAWL RESULT ==========");
            System.out.println("URL:      " + doc.getUrl());
            System.out.println("Title:    " + doc.getTitle());
            System.out.println("Status:   " + doc.getHttpStatusCode());

            String preview = doc.getCleanedText();
            if (preview.length() > 200) {
                preview = preview.substring(0, 200) + "...";
            }
            System.out.println("Text:     " + preview);
            System.out.println("Links:    " + doc.getOutLinks().size());

            if (!doc.getOutLinks().isEmpty()) {
                System.out.println("\nFirst 5 links:");
                doc.getOutLinks().stream().limit(5).forEach(System.out::println);
            }

            System.out.println("\n✅ Crawler working correctly!");

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}