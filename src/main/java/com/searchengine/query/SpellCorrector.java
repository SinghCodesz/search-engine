package com.searchengine.query;

import com.searchengine.indexer.InvertedIndex;

import java.util.*;

/**
 * Suggests spelling corrections using Levenshtein edit distance.
 * Like Google's "Did you mean..." feature.
 */
public class SpellCorrector {

    private final InvertedIndex index;
    private final Set<String> dictionary; // All known terms from the index
    private static final int MAX_SUGGESTIONS = 3;
    private static final int MAX_EDIT_DISTANCE = 2;

    public SpellCorrector(InvertedIndex index) {
        this.index = index;
        this.dictionary = new HashSet<>(index.getAllTerms());
    }

    /**
     * Find the best spelling correction for a query.
     * Returns the corrected query or null if no good correction found.
     */
    public String correctQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            return null;
        }

        String[] words = query.toLowerCase().trim().split("\\s+");
        StringBuilder corrected = new StringBuilder();
        boolean changed = false;

        for (String word : words) {
            // Skip stop words (they're usually correct)
            if (isStopWord(word)) {
                corrected.append(word).append(" ");
                continue;
            }

            // If the word exists in our dictionary, it's correct
            if (dictionary.contains(word)) {
                corrected.append(word).append(" ");
                continue;
            }

            // Word not in dictionary — find the best correction
            String suggestion = findBestMatch(word);
            if (suggestion != null) {
                corrected.append(suggestion).append(" ");
                changed = true;
            } else {
                corrected.append(word).append(" "); // Keep original if no match
            }
        }

        String result = corrected.toString().trim();

        // Only return if we actually changed something
        return changed ? result : null;
    }

    /**
     * Find the best matching word from the dictionary.
     * Uses Levenshtein distance with frequency-based tiebreaking.
     */
    private String findBestMatch(String word) {
        if (word.length() < 2) return null; // Too short to correct

        // Priority queue: [distance, frequency, word]
        PriorityQueue<Candidate> candidates = new PriorityQueue<>(
                Comparator.comparingInt(Candidate::getDistance)
                        .thenComparing(Comparator.comparingInt(Candidate::getFrequency).reversed())
        );

        // Compare against every dictionary word
        for (String dictWord : dictionary) {
            // Quick filter: skip if length difference > MAX_EDIT_DISTANCE
            if (Math.abs(dictWord.length() - word.length()) > MAX_EDIT_DISTANCE + 1) {
                continue;
            }

            int distance = levenshteinDistance(word, dictWord);

            if (distance <= MAX_EDIT_DISTANCE) {
                int frequency = index.getDocumentFrequency(dictWord);
                candidates.add(new Candidate(dictWord, distance, frequency));
            }
        }

        if (candidates.isEmpty()) {
            return null;
        }

        // Return the best candidate
        return candidates.peek().getWord();
    }

    /**
     * Get multiple suggestions for a word.
     */
    public List<String> getSuggestions(String word) {
        if (word == null || word.length() < 2) {
            return new ArrayList<>();
        }

        List<Candidate> candidates = new ArrayList<>();

        for (String dictWord : dictionary) {
            if (Math.abs(dictWord.length() - word.length()) > MAX_EDIT_DISTANCE + 1) {
                continue;
            }

            int distance = levenshteinDistance(word, dictWord);
            if (distance <= MAX_EDIT_DISTANCE) {
                int frequency = index.getDocumentFrequency(dictWord);
                candidates.add(new Candidate(dictWord, distance, frequency));
            }
        }

        // Sort by distance, then by frequency
        candidates.sort(Comparator.comparingInt(Candidate::getDistance)
                .thenComparing(Comparator.comparingInt(Candidate::getFrequency).reversed()));

        List<String> suggestions = new ArrayList<>();
        for (int i = 0; i < Math.min(MAX_SUGGESTIONS, candidates.size()); i++) {
            suggestions.add(candidates.get(i).getWord());
        }

        return suggestions;
    }

    /**
     * Calculate Levenshtein (Edit) Distance between two strings.
     * Minimum number of single-character edits (insert, delete, substitute)
     * needed to change word1 into word2.
     */
    public static int levenshteinDistance(String word1, String word2) {
        int m = word1.length();
        int n = word2.length();

        // dp[i][j] = distance between word1[0..i-1] and word2[0..j-1]
        int[][] dp = new int[m + 1][n + 1];

        // Base cases: transforming to/from empty string
        for (int i = 0; i <= m; i++) {
            dp[i][0] = i; // Delete all characters
        }
        for (int j = 0; j <= n; j++) {
            dp[0][j] = j; // Insert all characters
        }

        // Fill the DP table
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (word1.charAt(i - 1) == word2.charAt(j - 1)) {
                    // Characters match — no edit needed
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    // Characters differ — take minimum of insert, delete, substitute
                    dp[i][j] = 1 + Math.min(
                            dp[i - 1][j],     // Delete from word1
                            Math.min(
                                    dp[i][j - 1],     // Insert into word1
                                    dp[i - 1][j - 1]  // Substitute
                            )
                    );
                }
            }
        }

        return dp[m][n];
    }

    /**
     * Check if a word is a common stop word.
     * These don't need spell checking.
     */
    private boolean isStopWord(String word) {
        String[] stopWords = {"the", "a", "an", "and", "or", "but", "in", "on",
                "at", "to", "for", "of", "with", "by", "is", "are",
                "was", "were", "be", "been", "being", "have", "has",
                "had", "do", "does", "did", "will", "would", "could",
                "should", "may", "might", "must", "can", "not", "no"};
        return Arrays.asList(stopWords).contains(word.toLowerCase());
    }

    /**
     * Get the size of the dictionary.
     */
    public int getDictionarySize() {
        return dictionary.size();
    }

    /**
     * Internal class to hold candidate corrections.
     */
    private static class Candidate {
        private final String word;
        private final int distance;
        private final int frequency;

        Candidate(String word, int distance, int frequency) {
            this.word = word;
            this.distance = distance;
            this.frequency = frequency;
        }

        String getWord() { return word; }
        int getDistance() { return distance; }
        int getFrequency() { return frequency; }
    }
}