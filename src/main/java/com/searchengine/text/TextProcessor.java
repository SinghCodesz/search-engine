package com.searchengine.text;

import java.util.List;

/**
 * Complete text processing pipeline.
 * Tokenize → Remove Stop Words → Stem
 */
public class TextProcessor {

    private final Tokenizer tokenizer;
    private final StopWordFilter stopWordFilter;
    private final PorterStemmer stemmer;

    public TextProcessor() {
        this.tokenizer = new Tokenizer();
        this.stopWordFilter = new StopWordFilter();
        this.stemmer = new PorterStemmer();
    }

    /**
     * Process raw text into clean, searchable tokens.
     */
    public List<String> process(String text) {
        // Step 1: Tokenize
        List<String> tokens = tokenizer.tokenize(text);

        // Step 2: Remove stop words
        tokens = stopWordFilter.removeStopWords(tokens);

        // Step 3: Stem to root form
        tokens = stemmer.stemTokens(tokens);

        return tokens;
    }

    /**
     * Process text and return unique tokens only.
     */
    public List<String> processUnique(String text) {
        List<String> tokens = process(text);
        return new java.util.ArrayList<>(new java.util.HashSet<>(tokens));
    }

    /**
     * Get statistics about text processing.
     */
    public String getStats(String text) {
        List<String> rawTokens = tokenizer.tokenize(text);
        List<String> afterStopWords = stopWordFilter.removeStopWords(rawTokens);
        List<String> afterStemming = stemmer.stemTokens(afterStopWords);

        return String.format(
                "Raw tokens: %d → After stop words: %d → After stemming: %d (unique: %d)",
                rawTokens.size(),
                afterStopWords.size(),
                afterStemming.size(),
                new java.util.HashSet<>(afterStemming).size()
        );
    }
}