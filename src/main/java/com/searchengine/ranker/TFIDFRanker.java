package com.searchengine.ranker;

import com.searchengine.indexer.InvertedIndex;
import com.searchengine.indexer.Posting;
import com.searchengine.text.TextProcessor;

import java.util.*;

/**
 * TF-IDF Ranking Algorithm.
 *
 * TF (Term Frequency) = How often does the term appear in this document?
 *   TF = (frequency of term in document) / (total terms in document)
 *
 * IDF (Inverse Document Frequency) = How rare is this term across all documents?
 *   IDF = log(totalDocuments / documentsContainingTerm)
 *
 * TF-IDF = TF × IDF
 *
 * High TF-IDF means:
 *   - Term appears often in this document (high TF)
 *   - Term is rare across all documents (high IDF)
 *   = This document is highly relevant for this term
 */
public class TFIDFRanker implements Ranker {

    private InvertedIndex index;
    private TextProcessor textProcessor;
    private int totalDocuments;

    public TFIDFRanker() {
        this.textProcessor = new TextProcessor();
    }

    public TFIDFRanker(InvertedIndex index) {
        this();
        setIndex(index);
    }

    @Override
    public void setIndex(InvertedIndex index) {
        this.index = index;
        this.totalDocuments = index.getTotalDocuments();
    }

    @Override
    public List<DocumentScore> rank(String query, List<Integer> documentIds) {
        // Process query through the same text pipeline
        List<String> queryTerms = textProcessor.process(query);

        if (queryTerms.isEmpty()) {
            return new ArrayList<>();
        }

        // Calculate score for each candidate document
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

        // Sort by score descending
        Collections.sort(results);

        return results;
    }

    @Override
    public double score(String term, int documentId) {
        double tf = calculateTF(term, documentId);
        double idf = calculateIDF(term);
        return tf * idf;
    }

    /**
     * Calculate Term Frequency.
     * TF = (occurrences of term in doc) / (total terms in doc)
     */
    private double calculateTF(String term, int documentId) {
        List<Posting> postings = index.getPostings(term);
        if (postings == null) return 0.0;

        for (Posting p : postings) {
            if (p.getDocumentId() == documentId) {
                // Simple TF: just the frequency
                // We don't have document length stored, so use raw frequency
                return p.getTermFrequency();
            }
        }
        return 0.0;
    }

    /**
     * Calculate Inverse Document Frequency.
     * IDF = log(totalDocs / docsContainingTerm)
     */
    private double calculateIDF(String term) {
        int docFrequency = index.getDocumentFrequency(term);
        if (docFrequency == 0) return 0.0;

        // Use log base 10 for interpretable scores
        // Add 1 to avoid log(1) = 0 for terms in all documents
        return Math.log10((double) totalDocuments / docFrequency) + 1.0;
    }

    /**
     * Get query terms for debugging.
     */
    public List<String> getQueryTerms(String query) {
        return textProcessor.process(query);
    }
}