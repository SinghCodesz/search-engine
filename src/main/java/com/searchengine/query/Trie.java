package com.searchengine.query;

import java.util.*;

/**
 * Trie (Prefix Tree) data structure for autocomplete.
 *
 * Supports:
 * - Insert words with frequencies
 * - Search by prefix
 * - Get top K suggestions by frequency
 */
public class Trie {

    private final TrieNode root;
    private static final int MAX_SUGGESTIONS = 8;

    public Trie() {
        this.root = new TrieNode();
    }

    /**
     * Insert a word with its document frequency.
     */
    public void insert(String word, int frequency) {
        TrieNode current = root;
        for (char c : word.toLowerCase().toCharArray()) {
            current.children.putIfAbsent(c, new TrieNode());
            current = current.children.get(c);
        }
        current.isEndOfWord = true;
        current.frequency = frequency;
        current.word = word;
    }

    /**
     * Get autocomplete suggestions for a prefix.
     * Returns top K words by frequency.
     */
    public List<String> getSuggestions(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return new ArrayList<>();
        }

        // Navigate to the node representing the prefix
        TrieNode current = root;
        for (char c : prefix.toLowerCase().toCharArray()) {
            if (!current.children.containsKey(c)) {
                return new ArrayList<>(); // Prefix not found
            }
            current = current.children.get(c);
        }

        // Collect all words under this node
        List<TrieNode> allWords = new ArrayList<>();
        collectAllWords(current, allWords);

        // Sort by frequency (highest first), then alphabetically
        allWords.sort((a, b) -> {
            if (b.frequency != a.frequency) {
                return Integer.compare(b.frequency, a.frequency);
            }
            return a.word.compareTo(b.word);
        });

        // Return top K
        List<String> suggestions = new ArrayList<>();
        for (int i = 0; i < Math.min(MAX_SUGGESTIONS, allWords.size()); i++) {
            suggestions.add(allWords.get(i).word);
        }

        return suggestions;
    }

    /**
     * Get suggestions with their frequencies.
     */
    public List<Map.Entry<String, Integer>> getSuggestionsWithFrequency(String prefix) {
        List<String> words = getSuggestions(prefix);
        List<Map.Entry<String, Integer>> result = new ArrayList<>();

        for (String word : words) {
            int freq = getFrequency(word);
            result.add(new AbstractMap.SimpleEntry<>(word, freq));
        }

        return result;
    }

    /**
     * Get frequency of a word.
     */
    public int getFrequency(String word) {
        TrieNode current = root;
        for (char c : word.toLowerCase().toCharArray()) {
            if (!current.children.containsKey(c)) {
                return 0;
            }
            current = current.children.get(c);
        }
        return current.isEndOfWord ? current.frequency : 0;
    }

    /**
     * Recursively collect all complete words from a node.
     */
    private void collectAllWords(TrieNode node, List<TrieNode> result) {
        if (node.isEndOfWord) {
            result.add(node);
        }
        for (TrieNode child : node.children.values()) {
            collectAllWords(child, result);
        }
    }

    /**
     * Get total number of words in the trie.
     */
    public int size() {
        List<TrieNode> allWords = new ArrayList<>();
        collectAllWords(root, allWords);
        return allWords.size();
    }

    /**
     * Check if a word exists in the trie.
     */
    public boolean contains(String word) {
        TrieNode current = root;
        for (char c : word.toLowerCase().toCharArray()) {
            if (!current.children.containsKey(c)) {
                return false;
            }
            current = current.children.get(c);
        }
        return current.isEndOfWord;
    }

    /**
     * Trie Node.
     */
    private static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        boolean isEndOfWord = false;
        int frequency = 0;
        String word = "";
    }
}