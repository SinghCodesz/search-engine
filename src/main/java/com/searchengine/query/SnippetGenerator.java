package com.searchengine.query;

import com.searchengine.indexer.InvertedIndex;
import com.searchengine.indexer.Posting;

import java.util.*;

/**
 * Generates contextual snippets around query terms in search results.
 * Like Google's search result snippets.
 */
public class SnippetGenerator {

    private final InvertedIndex index;
    private static final int SNIPPET_RADIUS = 100; // Characters around the match

    public SnippetGenerator(InvertedIndex index) {
        this.index = index;
    }

    /**
     * Generate a snippet for a document containing the query terms.
     * Returns a short text excerpt with query terms highlighted.
     */
    public String generateSnippet(String documentText, List<String> queryTerms) {
        if (documentText == null || documentText.isEmpty()) {
            return "";
        }

        if (queryTerms == null || queryTerms.isEmpty()) {
            return truncate(documentText, SNIPPET_RADIUS * 2);
        }

        // Find the position of the first occurrence of any query term
        int bestPosition = findBestSnippetPosition(documentText, queryTerms);

        if (bestPosition < 0) {
            return truncate(documentText, SNIPPET_RADIUS * 2);
        }

        return extractSnippet(documentText, bestPosition, queryTerms);
    }

    /**
     * Generate a snippet using positional index data.
     * More accurate than text scanning — uses exact positions from index.
     */
    public String generateSnippetFromIndex(int documentId, String documentText,
                                           List<String> queryTerms) {
        if (documentText == null || documentText.isEmpty()) {
            return "";
        }

        // Try to find positions from the index first
        for (String term : queryTerms) {
            List<Posting> postings = index.getPostings(term);
            if (postings != null) {
                for (Posting p : postings) {
                    if (p.getDocumentId() == documentId && !p.getPositions().isEmpty()) {
                        // Convert word position to approximate character position
                        int charPos = estimateCharPosition(documentText, p.getPositions().get(0));
                        return extractSnippet(documentText, charPos, queryTerms);
                    }
                }
            }
        }

        // Fallback: scan the text
        return generateSnippet(documentText, queryTerms);
    }

    /**
     * Find the best position in the document to center the snippet.
     * Tries to find a region with multiple query terms.
     */
    private int findBestSnippetPosition(String text, List<String> queryTerms) {
        String lowerText = text.toLowerCase();
        int bestPosition = -1;
        int bestScore = -1;

        for (String term : queryTerms) {
            int pos = lowerText.indexOf(term.toLowerCase());
            while (pos >= 0) {
                // Count how many other query terms are nearby
                int score = countNearbyTerms(lowerText, queryTerms, pos, SNIPPET_RADIUS);
                if (score > bestScore) {
                    bestScore = score;
                    bestPosition = pos;
                }
                pos = lowerText.indexOf(term.toLowerCase(), pos + 1);
            }
        }

        return bestPosition;
    }

    /**
     * Count query terms within a radius of a position.
     */
    private int countNearbyTerms(String text, List<String> queryTerms,
                                 int center, int radius) {
        int count = 0;
        int start = Math.max(0, center - radius);
        int end = Math.min(text.length(), center + radius);
        String region = text.substring(start, end);

        for (String term : queryTerms) {
            if (region.contains(term.toLowerCase())) {
                count++;
            }
        }
        return count;
    }

    /**
     * Extract a snippet centered around a position.
     */
    private String extractSnippet(String text, int center, List<String> queryTerms) {
        int start = Math.max(0, center - SNIPPET_RADIUS);
        int end = Math.min(text.length(), center + SNIPPET_RADIUS);

        // Adjust start to not break words
        if (start > 0) {
            while (start < text.length() && text.charAt(start) != ' ' &&
                    text.charAt(start) != '.' && text.charAt(start) != '\n') {
                start++;
            }
        }

        // Adjust end to not break words
        if (end < text.length()) {
            while (end > 0 && text.charAt(end) != ' ' &&
                    text.charAt(end) != '.' && text.charAt(end) != '\n') {
                end--;
            }
        }

        String snippet = text.substring(start, end).trim();

        // Highlight query terms
        for (String term : queryTerms) {
            snippet = highlightTerm(snippet, term);
        }

        // Add ellipsis
        String prefix = start > 0 ? "..." : "";
        String suffix = end < text.length() ? "..." : "";

        return prefix + snippet + suffix;
    }

    /**
     * Highlight a term in text by wrapping it with markers.
     */
    private String highlightTerm(String text, String term) {
        // Case-insensitive replacement
        return text.replaceAll("(?i)" + java.util.regex.Pattern.quote(term),
                "<b>$0</b>");
    }

    /**
     * Estimate character position from word position.
     * Rough approximation: average word length + space.
     */
    private int estimateCharPosition(String text, int wordPosition) {
        String[] words = text.split("\\s+");
        int charCount = 0;
        int wordsCounted = 0;

        for (String word : words) {
            if (wordsCounted >= wordPosition) break;
            charCount += word.length() + 1; // +1 for space
            wordsCounted++;
        }

        return Math.min(charCount, text.length() - 1);
    }

    /**
     * Truncate text to a maximum length.
     */
    private String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength).trim() + "...";
    }

    /**
     * Get snippet radius setting.
     */
    public int getSnippetRadius() {
        return SNIPPET_RADIUS;
    }
}