package com.searchengine.ranker;

/**
 * Stores a document ID with its relevance score.
 * Used for sorting search results.
 */
public class DocumentScore implements Comparable<DocumentScore> {
    private int documentId;
    private double score;

    public DocumentScore(int documentId, double score) {
        this.documentId = documentId;
        this.score = score;
    }

    public int getDocumentId() { return documentId; }
    public double getScore() { return score; }

    @Override
    public int compareTo(DocumentScore other) {
        // Sort by score descending (higher score first)
        return Double.compare(other.score, this.score);
    }

    @Override
    public String toString() {
        return String.format("DocScore(id=%d, score=%.4f)", documentId, score);
    }
}