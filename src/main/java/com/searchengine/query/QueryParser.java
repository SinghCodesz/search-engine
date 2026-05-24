package com.searchengine.query;

import com.searchengine.text.TextProcessor;

import java.util.*;

/**
 * Parses user queries into searchable terms.
 * Supports AND (default) and OR queries.
 */
public class QueryParser {

    private final TextProcessor textProcessor;

    public QueryParser() {
        this.textProcessor = new TextProcessor();
    }

    /**
     * Parse a user query into processed tokens.
     * "The quick brown foxes" → ["quick", "brown", "fox"]
     */
    public List<String> parseQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return textProcessor.process(query);
    }

    /**
     * Check if query contains explicit OR operator.
     * "machine OR learning" → OR query
     * "machine learning" → AND query (default)
     */
    public boolean isOrQuery(String query) {
        return query.toUpperCase().contains(" OR ");
    }

    /**
     * Split query by OR operator.
     */
    public List<String> splitOrQuery(String query) {
        return Arrays.asList(query.toUpperCase().split(" OR "));
    }

    /**
     * Get unique terms from query.
     */
    public Set<String> getUniqueTerms(String query) {
        return new HashSet<>(parseQuery(query));
    }
}