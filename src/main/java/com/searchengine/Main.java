package com.searchengine;

import com.searchengine.crawler.MultiThreadedCrawler;
import com.searchengine.model.Document;
import com.searchengine.text.TextProcessor;

import java.util.List;

public class Main {
    public static void main(String[] args) {

        // Crawl 50 pages
        MultiThreadedCrawler crawler = new MultiThreadedCrawler(50, 5);
        crawler.addSeed("https://example.com");
        crawler.addSeed("https://httpbin.org");
        crawler.addSeed("https://books.toscrape.com");
        crawler.addSeed("https://quotes.toscrape.com");

        System.out.println("Starting web crawl...\n");
        List<Document> documents = crawler.startCrawling();

        // Process all documents through text pipeline
        System.out.println("\n========== TEXT PROCESSING ==========");
        TextProcessor processor = new TextProcessor();

        int totalTokens = 0;
        int totalUniqueTokens = 0;

        for (Document doc : documents) {
            List<String> tokens = processor.process(doc.getCleanedText());
            totalTokens += tokens.size();
            totalUniqueTokens += new java.util.HashSet<>(tokens).size();
        }

        System.out.println("Documents processed: " + documents.size());
        System.out.println("Total tokens: " + totalTokens);
        System.out.println("Total unique tokens: " + totalUniqueTokens);
        System.out.println("Avg tokens per document: " + (totalTokens / documents.size()));

        // Show sample output for first document
        if (!documents.isEmpty()) {
            Document first = documents.get(0);
            System.out.println("\nSample (first document):");
            System.out.println("Title: " + first.getTitle());
            System.out.println("Raw text length: " + first.getCleanedText().length());
            List<String> sampleTokens = processor.process(first.getCleanedText());
            System.out.println("Tokens: " + sampleTokens.size());
            System.out.println("First 20 tokens: " + sampleTokens.subList(0,
                    Math.min(20, sampleTokens.size())));
        }

        System.out.println("\n✅ Text processing pipeline complete!");
    }
}