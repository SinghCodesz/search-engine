package com.searchengine;

import com.searchengine.indexer.IndexSerializer;
import com.searchengine.indexer.InvertedIndex;
import com.searchengine.query.SearchService;
import com.searchengine.ranker.DocumentScore;

import java.util.*;

public class Main {
    public static void main(String[] args) {

        IndexSerializer serializer = new IndexSerializer();
        InvertedIndex index = serializer.loadIndex();
        Map<Integer, String[]> docMap = serializer.loadDocumentMap();

        if (index.getTotalDocuments() == 0 && docMap.isEmpty()) {
            System.out.println("No saved index found. Run Day 5 first to build the index.");
            return;
        }

        System.out.println("Index loaded: " + index.getTotalDocuments() +
                " documents, " + index.getVocabularySize() + " unique terms");

        SearchService searchService = new SearchService(index);

        // Test multi-word searches
        System.out.println("\n" + "=".repeat(60));
        System.out.println("MULTI-WORD SEARCH TESTS");
        System.out.println("=".repeat(60));

        // Test 1: AND query (both words must be present)
        testSearch(searchService, docMap, "book scrap");

        // Test 2: AND query (common words)
        testSearch(searchService, docMap, "life love");

        // Test 3: Single word
        testSearch(searchService, docMap, "domain");

        // Test 4: Rare word search
        testSearch(searchService, docMap, "documentation");

        // Test 5: Term that doesn't exist
        testSearch(searchService, docMap, "xyznonexistent");

        System.out.println("\n✅ Multi-word query engine working!");
    }

    private static void testSearch(SearchService service,
                                   Map<Integer, String[]> docMap,
                                   String query) {
        List<DocumentScore> results = service.search(query);

        if (results.isEmpty()) {
            System.out.println("\nQuery '" + query + "': No results");
            return;
        }

        System.out.println("\nTop " + Math.min(5, results.size()) + " results:");
        results.stream().limit(5).forEach(ds -> {
            String[] docInfo = docMap.get(ds.getDocumentId());
            String title = docInfo != null ? docInfo[1] : "Unknown";
            String url = docInfo != null ? docInfo[0] : "Unknown";
            System.out.println(String.format("  %.4f — %s", ds.getScore(), title));
            System.out.println("         " + url);
        });
    }
}