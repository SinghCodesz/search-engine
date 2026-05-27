package com.searchengine;

import com.searchengine.indexer.IndexSerializer;
import com.searchengine.indexer.InvertedIndex;
import com.searchengine.ranker.*;
import com.searchengine.query.SearchService;
import java.util.List;
import java.util.Map;
import com.searchengine.text.TextProcessor;

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

        // Create both rankers
        TFIDFRanker tfidfRanker = new TFIDFRanker(index);
        BM25Ranker bm25Ranker = new BM25Ranker(index);

        System.out.println("\nBM25 Parameters: " + bm25Ranker.getParameters());

        // Compare TF-IDF vs BM25
        System.out.println("\n" + "=".repeat(70));
        System.out.println("TF-IDF vs BM25 COMPARISON");
        System.out.println("=".repeat(70));

        compareRankers(tfidfRanker, bm25Ranker, index, docMap, "book");
        compareRankers(tfidfRanker, bm25Ranker, index, docMap, "book scrap");
        compareRankers(tfidfRanker, bm25Ranker, index, docMap, "quot");

        // Show BM25 explanation
        System.out.println("\n========== BM25 EXPLANATION ==========");
        explainBM25(index, bm25Ranker, "book");
        // Test phrase queries
        System.out.println("\n" + "=".repeat(70));
        System.out.println("PHRASE QUERY TESTS");
        System.out.println("=".repeat(70));

        SearchService searchService = new SearchService(index);

        // Note: Your index might not have many phrases. Test with words that
        // appear together in your crawled documents.
        testSearch(searchService, docMap, "\"example domain\"");
        testSearch(searchService, docMap, "\"books toscrape\"");
        testSearch(searchService, docMap, "\"quotes toscrape\"");
        testSearch(searchService, docMap, "\"goodreads com\"");

        System.out.println("\n✅ BM25 ranking working!");
    }

    private static Map<Integer, String> loadDocumentTexts(InvertedIndex index,
                                                          IndexSerializer serializer,
                                                          Map<Integer, String[]> docMap) {
        Map<Integer, String> texts = new HashMap<>();
        // We don't store full text in the serialized index.
        // For now, use the stored document map titles and URLs as fallback.
        // The snippet will scan whatever text is available.
        for (Map.Entry<Integer, String[]> entry : docMap.entrySet()) {
            int docId = entry.getKey();
            String[] info = entry.getValue();
            // Use title + URL as searchable text (limited but works)
            texts.put(docId, info[1] + " " + info[0]);
        }
        return texts;
    }

    private static void testSearch(SearchService service,
                                   Map<Integer, String[]> docMap,
                                   String query) {
        List<com.searchengine.ranker.DocumentScore> results = service.search(query);

        if (results.isEmpty()) {
            System.out.println("\nQuery '" + query + "': No results");
            return;
        }

        System.out.println("\nTop " + Math.min(5, results.size()) + " results:");

        // Get query terms for snippet generation
        List<String> queryTerms = new com.searchengine.text.TextProcessor().process(
                query.replace("\"", ""));

        results.stream().limit(5).forEach(ds -> {
            String[] docInfo = docMap.get(ds.getDocumentId());
            String title = docInfo != null ? docInfo[1] : "Unknown";
            String url = docInfo != null ? docInfo[0] : "Unknown";

            System.out.println(String.format("  %.4f — %s", ds.getScore(), title));
            System.out.println("         " + url);

            // Generate a simple snippet using the title + url text
            String docText = title + " " + url;
            // Show first occurrence of any query term
            String lowerText = docText.toLowerCase();
            for (String term : queryTerms) {
                int pos = lowerText.indexOf(term);
                if (pos >= 0) {
                    int start = Math.max(0, pos - 30);
                    int end = Math.min(docText.length(), pos + term.length() + 30);
                    String snippet = docText.substring(start, end);
                    if (start > 0) snippet = "..." + snippet;
                    if (end < docText.length()) snippet = snippet + "...";
                    System.out.println("         \"" + snippet + "\"");
                    break;
                }
            }
            System.out.println();
        });
    }

    private static void compareRankers(TFIDFRanker tfidf, BM25Ranker bm25,
                                       InvertedIndex index,
                                       Map<Integer, String[]> docMap,
                                       String query) {
        System.out.println("\nQuery: '" + query + "'");

        // Get all documents containing query terms
        Set<Integer> candidateDocIds = new HashSet<>();
        for (String term : new com.searchengine.text.TextProcessor().process(query)) {
            candidateDocIds.addAll(index.search(term));
        }

        if (candidateDocIds.isEmpty()) {
            System.out.println("  No results.");
            return;
        }

        List<Integer> docIds = new ArrayList<>(candidateDocIds);

        // TF-IDF ranking
        List<DocumentScore> tfidfResults = tfidf.rank(query, docIds);

        // BM25 ranking
        List<DocumentScore> bm25Results = bm25.rank(query, docIds);

        // Show top 5 comparison
        System.out.println(String.format("  %-5s %-30s %10s %10s",
                "Rank", "Document", "TF-IDF", "BM25"));
        System.out.println("  " + "-".repeat(58));

        int count = Math.min(5, Math.min(tfidfResults.size(), bm25Results.size()));

        // Create maps for quick lookup
        Map<Integer, Double> tfidfMap = new HashMap<>();
        Map<Integer, Double> bm25Map = new HashMap<>();

        for (int i = 0; i < tfidfResults.size(); i++) {
            DocumentScore ds = tfidfResults.get(i);
            tfidfMap.put(ds.getDocumentId(), ds.getScore());
        }
        for (int i = 0; i < bm25Results.size(); i++) {
            DocumentScore ds = bm25Results.get(i);
            bm25Map.put(ds.getDocumentId(), ds.getScore());
        }

        // Show BM25 top 5 with TF-IDF comparison
        for (int i = 0; i < count; i++) {
            DocumentScore bmScore = bm25Results.get(i);
            int docId = bmScore.getDocumentId();
            String[] docInfo = docMap.get(docId);
            String title = docInfo != null ? docInfo[1] : "Unknown";
            if (title.length() > 28) title = title.substring(0, 27) + "...";

            double tfidfScore = tfidfMap.getOrDefault(docId, 0.0);

            System.out.println(String.format("  #%d    %-30s %10.4f %10.4f",
                    (i + 1), title, tfidfScore, bmScore.getScore()));
        }
    }

    private static void explainBM25(InvertedIndex index, BM25Ranker bm25, String term) {
        System.out.println("Term: '" + term + "'");
        System.out.println("BM25 Parameters: " + bm25.getParameters());

        int docFreq = index.getDocumentFrequency(term);
        System.out.println("Documents containing '" + term + "': " + docFreq);

        List<Integer> docIds = index.search(term);
        System.out.println("\nTop documents by BM25 score:");
        System.out.println(String.format("  %-5s %-8s %-8s %-10s",
                "DocID", "TF", "DocLen", "BM25"));

        docIds.stream()
                .sorted((a, b) -> Double.compare(bm25.score(term, b), bm25.score(term, a)))
                .limit(5)
                .forEach(docId -> {
                    double score = bm25.score(term, docId);
                    // Get TF
                    double tf = 0;
                    for (var p : index.getPostings(term)) {
                        if (p.getDocumentId() == docId) {
                            tf = p.getTermFrequency();
                            break;
                        }
                    }
                    System.out.println(String.format("  %-5d %-8.0f %-8s %-10.4f",
                            docId, tf, "~", score));
                });

        System.out.println("\nBM25 Formula: IDF × (tf×(k1+1)) / (tf + k1×(1-b + b×docLen/avgDocLen))");
        System.out.println("  k1=1.2: Controls term frequency saturation");
        System.out.println("  b=0.75: Controls document length normalization");
        System.out.println("  100 occurrences is only slightly better than 10 (unlike TF-IDF)");
    }
}