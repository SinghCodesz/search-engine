package com.searchengine.text;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits text into individual words/tokens.
 * Lowercases everything and removes punctuation.
 */
public class Tokenizer {

    /**
     * Tokenize text into a list of cleaned words.
     */
    public List<String> tokenize(String text) {
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> tokens = new ArrayList<>();

        // Split on non-letter characters
        String[] words = text.toLowerCase().split("[^a-z0-9]+");

        for (String word : words) {
            word = word.trim();

            // Skip empty strings
            if (word.isEmpty()) {
                continue;
            }

            // Skip pure numbers (optional — you can keep them if you want)
            if (word.matches("\\d+")) {
                continue;
            }

            // Skip single characters (except 'a', 'i')
            if (word.length() == 1 && !word.equals("a") && !word.equals("i")) {
                continue;
            }

            tokens.add(word);
        }

        return tokens;
    }

    /**
     * Count total tokens in a document.
     */
    public int countTokens(String text) {
        return tokenize(text).size();
    }

    /**
     * Get unique tokens.
     */
    public List<String> getUniqueTokens(String text) {
        List<String> tokens = tokenize(text);
        return new ArrayList<>(new java.util.HashSet<>(tokens));
    }
}