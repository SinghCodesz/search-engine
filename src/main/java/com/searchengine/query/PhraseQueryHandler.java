package com.searchengine.query;

import com.searchengine.indexer.InvertedIndex;
import com.searchengine.indexer.Posting;

import java.util.*;

/**
 * Handles phrase queries like "machine learning".
 * Finds documents where query terms appear adjacent and in order.
 */
public class PhraseQueryHandler {

    private final InvertedIndex index;

    public PhraseQueryHandler(InvertedIndex index) {
        this.index = index;
    }

    /**
     * Execute a phrase query.
     * Returns document IDs where ALL terms appear consecutively in order.
     */
    public List<Integer> executePhraseQuery(List<String> terms) {
        if (terms == null || terms.size() < 2) {
            // Single term: just return its posting list
            return terms != null && terms.size() == 1 ?
                    index.search(terms.get(0)) : new ArrayList<>();
        }

        System.out.println("  Phrase query terms: " + terms);

        // Get posting lists for all terms
        List<List<Posting>> allPostings = new ArrayList<>();
        for (String term : terms) {
            List<Posting> postings = index.getPostings(term);
            if (postings == null || postings.isEmpty()) {
                return new ArrayList<>(); // Term not found
            }
            allPostings.add(postings);
        }

        // Start with documents containing the first term
        List<Integer> candidateDocs = getDocumentIds(allPostings.get(0));
        List<Integer> result = new ArrayList<>();

        // For each candidate document, check if it contains the phrase
        for (int docId : candidateDocs) {
            if (containsPhrase(docId, terms, allPostings)) {
                result.add(docId);
            }
        }

        return result;
    }

    /**
     * Check if a specific document contains all terms as a phrase.
     */
    private boolean containsPhrase(int docId, List<String> terms,
                                   List<List<Posting>> allPostings) {

        // Get positions of the first term in this document
        List<Integer> firstTermPositions = getPositions(docId, allPostings.get(0));

        if (firstTermPositions.isEmpty()) return false;

        // For each position of the first term, check if subsequent terms follow
        for (int startPos : firstTermPositions) {
            boolean phraseFound = true;

            // Check each subsequent term
            for (int i = 1; i < terms.size(); i++) {
                int expectedPosition = startPos + i;
                List<Integer> termPositions = getPositions(docId, allPostings.get(i));

                if (!termPositions.contains(expectedPosition)) {
                    phraseFound = false;
                    break;
                }
            }

            if (phraseFound) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get positions of a term in a specific document.
     */
    private List<Integer> getPositions(int docId, List<Posting> postings) {
        for (Posting p : postings) {
            if (p.getDocumentId() == docId) {
                return p.getPositions();
            }
        }
        return new ArrayList<>();
    }

    /**
     * Extract document IDs from a posting list.
     */
    private List<Integer> getDocumentIds(List<Posting> postings) {
        List<Integer> docIds = new ArrayList<>();
        for (Posting p : postings) {
            docIds.add(p.getDocumentId());
        }
        return docIds;
    }

    /**
     * Check if a query looks like a phrase query.
     * Simple heuristic: if it has multiple words and is quoted.
     */
    public static boolean isPhraseQuery(String query) {
        return query != null && query.startsWith("\"") && query.endsWith("\"");
    }

    /**
     * Strip quotes from a phrase query.
     * "machine learning" → machine learning
     */
    public static String stripQuotes(String query) {
        if (isPhraseQuery(query)) {
            return query.substring(1, query.length() - 1);
        }
        return query;
    }
}