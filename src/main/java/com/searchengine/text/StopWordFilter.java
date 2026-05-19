package com.searchengine.text;

import java.util.*;

/**
 * Removes common English stop words that don't add search value.
 * Words like "the", "is", "at", "which", etc.
 */
public class StopWordFilter {

    private final Set<String> stopWords;

    public StopWordFilter() {
        this.stopWords = new HashSet<>();
        loadDefaultStopWords();
    }

    /**
     * Load a standard list of English stop words.
     */
    private void loadDefaultStopWords() {
        // Common English stop words
        String[] words = {
                "a", "an", "the", "and", "or", "but", "in", "on", "at", "to", "for",
                "of", "with", "by", "from", "up", "about", "into", "through", "during",
                "before", "after", "above", "below", "between", "out", "off", "over",
                "under", "again", "further", "then", "once", "here", "there", "when",
                "where", "why", "how", "all", "both", "each", "few", "more", "most",
                "other", "some", "such", "no", "nor", "not", "only", "own", "same",
                "so", "than", "too", "very", "s", "t", "can", "will", "just", "don",
                "should", "now", "is", "am", "are", "was", "were", "be", "been", "being",
                "have", "has", "had", "having", "do", "does", "did", "doing", "would",
                "could", "shall", "should", "may", "might", "must", "need", "dare",
                "ought", "used", "it", "its", "itself", "they", "them", "their",
                "theirs", "themselves", "what", "which", "who", "whom", "this", "that",
                "these", "those", "i", "me", "my", "myself", "we", "our", "ours",
                "ourselves", "you", "your", "yours", "yourself", "yourselves", "he",
                "him", "his", "himself", "she", "her", "hers", "herself", "if"
        };

        stopWords.addAll(Arrays.asList(words));
    }

    /**
     * Check if a word is a stop word.
     */
    public boolean isStopWord(String word) {
        return stopWords.contains(word.toLowerCase());
    }

    /**
     * Remove all stop words from a list of tokens.
     */
    public List<String> removeStopWords(List<String> tokens) {
        List<String> filtered = new ArrayList<>();
        for (String token : tokens) {
            if (!isStopWord(token)) {
                filtered.add(token);
            }
        }
        return filtered;
    }

    /**
     * Add a custom stop word.
     */
    public void addStopWord(String word) {
        stopWords.add(word.toLowerCase());
    }

    /**
     * Get total number of stop words loaded.
     */
    public int size() {
        return stopWords.size();
    }
}