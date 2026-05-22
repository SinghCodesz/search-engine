package com.searchengine.indexer;

import com.searchengine.model.Document;
import com.searchengine.text.TextProcessor;

import java.util.*;

/**
 * Builds and stores an inverted index.
 * Maps terms to their postings lists.
 */
public class InvertedIndex {

    // term → list of postings
    private final Map<String, List<Posting>> index;
    private final TextProcessor textProcessor;
    private int totalDocuments;
    private int totalTokens;

    public InvertedIndex() {
        this.index = new HashMap<>();
        this.textProcessor = new TextProcessor();
        this.totalDocuments = 0;
        this.totalTokens = 0;
    }

    /**
     * Add a document to the index.
     * Tokenizes the document text and adds postings for each token.
     */
    public void addDocument(int documentId, String text) {
        List<String> tokens = textProcessor.process(text);

        int position = 0;
        for (String token : tokens) {
            addPosting(token, documentId, position);
            position++;
            totalTokens++;
        }

        totalDocuments++;
    }

    /**
     * Add a single posting (term occurrence) to the index.
     */
    private void addPosting(String term, int documentId, int position) {
        List<Posting> postingsList = index.get(term);

        if (postingsList == null) {
            // First time seeing this term
            postingsList = new ArrayList<>();
            index.put(term, postingsList);
        }

        // Check if this document already has a posting for this term
        Posting lastPosting = null;
        if (!postingsList.isEmpty()) {
            lastPosting = postingsList.get(postingsList.size() - 1);
        }

        if (lastPosting != null && lastPosting.getDocumentId() == documentId) {
            // Add position to existing posting
            lastPosting.addPosition(position);
        } else {
            // Create new posting for this document
            Posting newPosting = new Posting(documentId);
            newPosting.addPosition(position);
            postingsList.add(newPosting);
        }
    }


    /**
     * Directly add a posting (used when loading from disk).
     */
    public void addDocumentDirect(String term, int documentId,
                                  int termFrequency, List<Integer> positions) {
        List<Posting> postingsList = index.get(term);
        if (postingsList == null) {
            postingsList = new ArrayList<>();
            index.put(term, postingsList);
        }

        Posting posting = new Posting(documentId);
        for (int pos : positions) {
            posting.addPosition(pos);
        }
        postingsList.add(posting);
        totalTokens += termFrequency;
    }

    /**
     * Get postings list for a term.
     * Returns null if term doesn't exist in index.
     */
    public List<Posting> getPostings(String term) {
        return index.get(term.toLowerCase());
    }

    /**
     * Get the number of documents containing a term.
     */
    public int getDocumentFrequency(String term) {
        List<Posting> postings = index.get(term.toLowerCase());
        return postings == null ? 0 : postings.size();
    }

    /**
     * Get total number of unique terms in the index.
     */
    public int getVocabularySize() {
        return index.size();
    }

    /**
     * Get total number of documents indexed.
     */
    public int getTotalDocuments() {
        return totalDocuments;
    }

    public void setTotalDocuments(int count) {
        this.totalDocuments = count;
    }

    /**
     * Get total number of tokens indexed.
     */
    public int getTotalTokens() {
        return totalTokens;
    }

    /**
     * Get all terms in the index.
     */
    public Set<String> getAllTerms() {
        return index.keySet();
    }

    /**
     * Check if a term exists in the index.
     */
    public boolean containsTerm(String term) {
        return index.containsKey(term.toLowerCase());
    }

    /**
     * Search for a single term and return matching document IDs.
     */
    public List<Integer> search(String term) {
        List<Posting> postings = getPostings(term);
        if (postings == null) {
            return new ArrayList<>();
        }

        List<Integer> docIds = new ArrayList<>();
        for (Posting p : postings) {
            docIds.add(p.getDocumentId());
        }
        return docIds;
    }

    /**
     * Print statistics about the index.
     */
    public void printStats() {
        System.out.println("\n========== INDEX STATISTICS ==========");
        System.out.println("Documents indexed: " + totalDocuments);
        System.out.println("Total tokens: " + totalTokens);
        System.out.println("Unique terms: " + getVocabularySize());
        System.out.println("Avg tokens per document: " +
                (totalDocuments > 0 ? totalTokens / totalDocuments : 0));

        // Top 10 most frequent terms
        System.out.println("\nTop 10 most frequent terms:");
        index.entrySet().stream()
                .sorted((a, b) -> Integer.compare(
                        b.getValue().size(), a.getValue().size()))
                .limit(10)
                .forEach(entry -> {
                    int totalFreq = entry.getValue().stream()
                            .mapToInt(Posting::getTermFrequency).sum();
                    System.out.println("  " + entry.getKey() + " → " +
                            entry.getValue().size() + " docs, " + totalFreq + " occurrences");
                });

        // Top 10 rarest terms
        System.out.println("\nTop 10 rarest terms:");
        index.entrySet().stream()
                .sorted((a, b) -> Integer.compare(
                        a.getValue().size(), b.getValue().size()))
                .limit(10)
                .forEach(entry -> {
                    System.out.println("  " + entry.getKey() + " → " +
                            entry.getValue().size() + " docs");
                });
    }
}