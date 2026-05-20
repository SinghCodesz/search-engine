package com.searchengine.indexer;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents one occurrence of a term in a document.
 * Stores document ID, term frequency, and all positions.
 */
public class Posting {
    private int documentId;
    private int termFrequency;
    private List<Integer> positions;

    public Posting(int documentId) {
        this.documentId = documentId;
        this.termFrequency = 0;
        this.positions = new ArrayList<>();
    }

    public void addPosition(int position) {
        this.positions.add(position);
        this.termFrequency++;
    }

    public int getDocumentId() { return documentId; }
    public int getTermFrequency() { return termFrequency; }
    public List<Integer> getPositions() { return positions; }

    @Override
    public String toString() {
        return "Posting(docId=" + documentId + ", freq=" + termFrequency +
                ", positions=" + positions + ")";
    }
}