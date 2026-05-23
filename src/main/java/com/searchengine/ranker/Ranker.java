package com.searchengine.ranker;

import com.searchengine.indexer.InvertedIndex;
import java.util.List;

/**
 * Interface for ranking algorithms.
 * Different implementations: TF-IDF, BM25, etc.
 */
public interface Ranker {

    /**
     * Rank documents by relevance to the query.
     * Higher score = more relevant.
     */
    List<DocumentScore> rank(String query, List<Integer> documentIds);

    /**
     * Calculate score for a single term in a single document.
     */
    double score(String term, int documentId);

    /**
     * Set the inverted index (needed by all rankers).
     */
    void setIndex(InvertedIndex index);
}