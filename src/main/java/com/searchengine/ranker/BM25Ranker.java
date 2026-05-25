package com.searchengine.ranker;

import com.searchengine.indexer.InvertedIndex;
import com.searchengine.indexer.Posting;
import com.searchengine.text.TextProcessor;

import java.util.*;

/**
 * BM25 Ranking Algorithm (Okapi BM25).
 *
 * BM25 improves over TF-IDF with:
 * 1. Term saturation: extra occurrences add diminishing returns
 * 2. Document length normalization: short docs favored over long docs
 *
 * Formula:
 *   BM25(term, doc) = IDF × (tf × (k1 + 1)) / (tf + k1 × (1 - b + b × docLen/avgDocLen))
 *
 * Parameters:
 *   k1 = 1.2 (controls term frequency saturation, typical: 1.2-2.0)
 *   b = 0.75 (controls document length normalization, typical: 0.75)
 *
 * Used by: Elasticsearch, Lucene, most production search systems
 */
public class BM25Ranker implements Ranker {

    private InvertedIndex index;
    private TextProcessor textProcessor;
    private int totalDocuments;
    private double avgDocumentLength;

    // BM25 parameters
    private static final double K1 = 1.2;
    private static final double B = 0.75;

    public BM25Ranker() {
        this.textProcessor = new TextProcessor();
    }

    public BM25Ranker(InvertedIndex index) {
        this();
        setIndex(index);
    }

    @Override
    public void setIndex(InvertedIndex index) {
        this.index = index;
        this.totalDocuments = index.getTotalDocuments();
        this.avgDocumentLength = calculateAvgDocumentLength();
    }

    /**
     * Calculate average document length (in tokens).
     */
    private double calculateAvgDocumentLength() {
        if (totalDocuments == 0) return 1.0;
        return (double) index.getTotalTokens() / totalDocuments;
    }

    @Override
    public List<DocumentScore> rank(String query, List<Integer> documentIds) {
        List<String> queryTerms = textProcessor.process(query);

        if (queryTerms.isEmpty() || documentIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<DocumentScore> results = new ArrayList<>();

        for (int docId : documentIds) {
            double totalScore = 0.0;

            for (String term : queryTerms) {
                totalScore += score(term, docId);
            }

            if (totalScore > 0) {
                results.add(new DocumentScore(docId, totalScore));
            }
        }

        Collections.sort(results);
        return results;
    }

    @Override
    public double score(String term, int documentId) {
        double idf = calculateIDF(term);
        if (idf == 0.0) return 0.0;

        double tf = getTermFrequency(term, documentId);
        if (tf == 0.0) return 0.0;

        double docLength = getDocumentLength(documentId);
        double numerator = tf * (K1 + 1);
        double denominator = tf + K1 * (1 - B + B * docLength / avgDocumentLength);

        return idf * (numerator / denominator);
    }

    /**
     * Calculate IDF for BM25.
     * IDF = log((N - df + 0.5) / (df + 0.5) + 1)
     * where N = total documents, df = document frequency
     */
    private double calculateIDF(String term) {
        int docFrequency = index.getDocumentFrequency(term);
        if (docFrequency == 0) return 0.0;

        double idf = Math.log(
                (totalDocuments - docFrequency + 0.5) / (docFrequency + 0.5) + 1.0
        );

        return Math.max(0, idf);
    }

    /**
     * Get term frequency in a specific document.
     */
    private double getTermFrequency(String term, int documentId) {
        List<Posting> postings = index.getPostings(term);
        if (postings == null) return 0.0;

        for (Posting p : postings) {
            if (p.getDocumentId() == documentId) {
                return p.getTermFrequency();
            }
        }
        return 0.0;
    }

    /**
     * Get document length (total tokens in document).
     */
    private double getDocumentLength(int documentId) {
        // Estimate from index: sum of term frequencies for this document
        int totalTerms = 0;
        for (String term : index.getAllTerms()) {
            List<Posting> postings = index.getPostings(term);
            for (Posting p : postings) {
                if (p.getDocumentId() == documentId) {
                    totalTerms += p.getTermFrequency();
                    break; // Move to next term
                }
            }
        }
        return Math.max(1, totalTerms);
    }

    /**
     * Get query terms for debugging.
     */
    public List<String> getQueryTerms(String query) {
        return textProcessor.process(query);
    }

    /**
     * Get BM25 parameters.
     */
    public String getParameters() {
        return String.format("k1=%.1f, b=%.2f, avgDocLen=%.1f",
                K1, B, avgDocumentLength);
    }
}