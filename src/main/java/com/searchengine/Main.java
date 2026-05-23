package com.searchengine;

import com.searchengine.indexer.IndexSerializer;
import com.searchengine.indexer.InvertedIndex;
import com.searchengine.ranker.DocumentScore;
import com.searchengine.ranker.TFIDFRanker;

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

        // Create TF-IDF ranker
        TFIDFRanker ranker = new TFIDFRanker(index);

        // Test ranked searches
        System.out.println("\n========== RANKED SEARCHES ==========");

        testRankedSearch(index, ranker, docMap, "book");
        testRankedSearch(index, ranker, docMap, "life love");
        testRankedSearch(index, ranker, docMap, "world");
        testRankedSearch(index, ranker, docMap, "quot scrap");

        // Show TF-IDF explanation
        System.out.println("\n========== TF-IDF EXPLANATION ==========");
        explainTFIDF(index, ranker, "book");

        System.out.println("\n✅ TF-IDF ranking working!");
    }

    private static void testRankedSearch(InvertedIndex index, TFIDFRanker ranker,
                                         Map<Integer, String[]> docMap, String query) {
        System.out.println("\nQuery: '" + query + "'");

        List<String> queryTerms = ranker.getQueryTerms(query);
        System.out.println("Processed terms: " + queryTerms);

        // Find documents containing at least one query term
        Set<Integer> candidateDocIds = new HashSet<>();
        for (String term : queryTerms) {
            candidateDocIds.addAll(index.search(term));
        }

        if (candidateDocIds.isEmpty()) {
            System.out.println("  No results found.");
            return;
        }

        // Rank them
        List<DocumentScore> results = ranker.rank(query, new ArrayList<>(candidateDocIds));

        // Show top 5 results
        System.out.println("Top results:");
        results.stream().limit(5).forEach(ds -> {
            String[] docInfo = docMap.get(ds.getDocumentId());
            String title = docInfo != null ? docInfo[1] : "Unknown";
            System.out.println(String.format("  %.4f — %s (Doc %d)",
                    ds.getScore(), title, ds.getDocumentId()));
        });
    }

    private static void explainTFIDF(InvertedIndex index, TFIDFRanker ranker, String term) {
        System.out.println("Term: '" + term + "'");

        int docFreq = index.getDocumentFrequency(term);
        double idf = Math.log10((double) index.getTotalDocuments() / docFreq) + 1.0;

        System.out.println("  Documents containing '" + term + "': " + docFreq);
        System.out.println("  IDF = log(" + index.getTotalDocuments() + "/" + docFreq + ") + 1 = " + String.format("%.4f", idf));

        // Show TF for top documents
        List<Integer> docIds = index.search(term);
        System.out.println("  Top TF scores:");
        docIds.stream()
                .sorted((a, b) -> Double.compare(ranker.score(term, b), ranker.score(term, a)))
                .limit(5)
                .forEach(docId -> {
                    double tf = ranker.score(term, docId) / idf; // TF = TF-IDF / IDF
                    System.out.println(String.format("    Doc %d: TF=%.1f, TF-IDF=%.4f",
                            docId, tf, ranker.score(term, docId)));
                });
    }
}