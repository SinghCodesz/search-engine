package com.searchengine;

import com.searchengine.crawler.MultiThreadedCrawler;
import com.searchengine.indexer.IndexSerializer;
import com.searchengine.indexer.InvertedIndex;
import com.searchengine.model.Document;

import java.util.*;

public class Main {
    public static void main(String[] args) {

        IndexSerializer serializer = new IndexSerializer();

        // Try loading existing index
        InvertedIndex index = serializer.loadIndex();
        Map<Integer, String[]> docMap = serializer.loadDocumentMap();

        // Check if we actually loaded data
        if (index.getTotalDocuments() == 0 && docMap.isEmpty()) {
            System.out.println("No saved index found. Crawling and building new index...\n");

            // Crawl
            MultiThreadedCrawler crawler = new MultiThreadedCrawler(50, 5);
            crawler.addSeed("https://example.com");
            crawler.addSeed("https://books.toscrape.com");
            crawler.addSeed("https://quotes.toscrape.com");

            List<Document> documents = crawler.startCrawling();

            // Build index
            System.out.println("\n========== BUILDING INVERTED INDEX ==========");
            index = new InvertedIndex();
            docMap = new HashMap<>();

            for (int i = 0; i < documents.size(); i++) {
                Document doc = documents.get(i);
                int docId = i + 1;
                index.addDocument(docId, doc.getCleanedText());
                docMap.put(docId, new String[]{doc.getUrl(), doc.getTitle()});
            }

            // Save index to disk
            System.out.println("\n========== SAVING INDEX ==========");
            serializer.saveIndex(index, docMap);
            System.out.println("Index saved. Next startup will load from disk.");
        } else {
            System.out.println("Index loaded from disk. Skipping crawl.");
        }

        // Print stats
        index.printStats();

        // Test searches
        System.out.println("\n========== TEST SEARCHES ==========");
        testSearch(index, "book");
        testSearch(index, "life");
        testSearch(index, "love");

        // Show sample documents
        if (!docMap.isEmpty()) {
            System.out.println("\n========== SAMPLE DOCUMENTS ==========");
            docMap.entrySet().stream().limit(5).forEach(entry -> {
                System.out.println("Doc " + entry.getKey() + ": " + entry.getValue()[1]);
            });
        }

        System.out.println("\n✅ Index serialization working!");
    }

    private static void testSearch(InvertedIndex index, String term) {
        List<Integer> results = index.search(term);
        System.out.println("Search: '" + term + "' → " + results.size() + " documents");
    }
}