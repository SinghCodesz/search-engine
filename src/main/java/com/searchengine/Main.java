package com.searchengine;

import com.searchengine.crawler.MultiThreadedCrawler;
import com.searchengine.indexer.InvertedIndex;
import com.searchengine.model.Document;

import java.util.List;

public class Main {
    public static void main(String[] args) {

        // Crawl 50 pages
        MultiThreadedCrawler crawler = new MultiThreadedCrawler(50, 5);
        crawler.addSeed("https://example.com");
        crawler.addSeed("https://books.toscrape.com");
        crawler.addSeed("https://quotes.toscrape.com");

        System.out.println("Starting web crawl...\n");
        List<Document> documents = crawler.startCrawling();

        // Build inverted index
        System.out.println("\n========== BUILDING INVERTED INDEX ==========");
        InvertedIndex index = new InvertedIndex();

        for (int i = 0; i < documents.size(); i++) {
            Document doc = documents.get(i);
            index.addDocument(i + 1, doc.getCleanedText());
        }

        System.out.println("Index built successfully!");

        // Print statistics
        index.printStats();

        // Test searches
        System.out.println("\n========== TEST SEARCHES ==========");

        testSearch(index, "book");
        testSearch(index, "life");
        testSearch(index, "love");
        testSearch(index, "world");
        testSearch(index, "quot");

        System.out.println("\n✅ Inverted index working!");
    }

    private static void testSearch(InvertedIndex index, String term) {
        List<Integer> results = index.search(term);
        System.out.println("Search: '" + term + "' → " + results.size() +
                " documents found: " + results);
    }
}