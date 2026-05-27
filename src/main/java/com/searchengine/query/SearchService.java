package com.searchengine.query;

import com.searchengine.indexer.InvertedIndex;
import com.searchengine.ranker.DocumentScore;
import com.searchengine.ranker.TFIDFRanker;
import com.searchengine.query.PhraseQueryHandler;
import com.searchengine.query.SnippetGenerator;

import java.util.*;

/**
 * Coordinates the full search pipeline:
 * Parse → Retrieve → Intersect → Rank
 */
public class SearchService {

    private final InvertedIndex index;
    private final QueryParser queryParser;
    private final PostingsIntersector intersector;
    private final TFIDFRanker ranker;
    private final PhraseQueryHandler phraseHandler;
    private final SnippetGenerator snippetGenerator;

    public SearchService(InvertedIndex index) {
        this.index = index;
        this.queryParser = new QueryParser();
        this.intersector = new PostingsIntersector();
        this.ranker = new TFIDFRanker(index);
        this.phraseHandler = new PhraseQueryHandler(index);
        this.snippetGenerator = new SnippetGenerator(index);
    }

    /**
     * Execute a full search.
     * Returns ranked list of document scores.
     */
    public List<DocumentScore> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }

        System.out.println("\n========== SEARCH: '" + query + "' ==========");

        // Step 1: Parse query
        List<String> queryTerms = queryParser.parseQuery(query);
        System.out.println("Parsed terms: " + queryTerms);

        if (queryTerms.isEmpty()) {
            return new ArrayList<>();
        }

        // Step 2: Check for phrase query
        if (PhraseQueryHandler.isPhraseQuery(query)) {
            System.out.println("Query type: PHRASE");
            String phraseContent = PhraseQueryHandler.stripQuotes(query);
            List<String> phraseTerms = queryParser.parseQuery(phraseContent);

            List<Integer> phraseResults = phraseHandler.executePhraseQuery(phraseTerms);

            if (phraseResults.isEmpty()) {
                System.out.println("No phrase matches found.");
                return new ArrayList<>();
            }

            System.out.println("Phrase matches: " + phraseResults.size());

            // Rank the phrase results
            List<DocumentScore> ranked = ranker.rank(phraseContent, phraseResults);
            System.out.println("Ranked results: " + ranked.size());
            return ranked;
        }

        // Step 2: Retrieve posting lists
        Map<String, List<Integer>> termPostings = new HashMap<>();
        for (String term : queryTerms) {
            List<Integer> postings = index.search(term);
            System.out.println("  '" + term + "' → " + postings.size() + " docs");
            if (!postings.isEmpty()) {
                termPostings.put(term, postings);
            }
        }

        if (termPostings.isEmpty()) {
            System.out.println("No matching documents found.");
            return new ArrayList<>();
        }

        // Step 3: Intersect posting lists (AND by default)
        List<List<Integer>> postingLists = new ArrayList<>(termPostings.values());
        List<Integer> resultDocIds;

        if (queryParser.isOrQuery(query)) {
            System.out.println("Query type: OR");
            resultDocIds = intersector.union(postingLists);
        } else {
            System.out.println("Query type: AND");
            resultDocIds = intersector.intersect(postingLists);
        }

        System.out.println("Documents after intersection: " + resultDocIds.size());

        if (resultDocIds.isEmpty()) {
            return new ArrayList<>();
        }

        // Step 4: Rank results
        List<DocumentScore> ranked = ranker.rank(query, resultDocIds);
        System.out.println("Ranked results: " + ranked.size());

        return ranked;
    }
    /**
     * Generate snippets for ranked results.
     */
    public Map<Integer, String> generateSnippets(String query,
                                                 List<DocumentScore> results,
                                                 Map<Integer, String> documentTexts) {
        Map<Integer, String> snippets = new HashMap<>();
        List<String> queryTerms = queryParser.parseQuery(query);

        for (DocumentScore ds : results) {
            int docId = ds.getDocumentId();
            String text = documentTexts.get(docId);
            if (text != null) {
                String snippet = snippetGenerator.generateSnippet(text, queryTerms);
                snippets.put(docId, snippet);
            }
        }

        return snippets;
    }

    /**
     * Get document count for a term.
     */
    public int getTermDocumentCount(String term) {
        return index.getDocumentFrequency(term);
    }

    /**
     * Get total indexed documents.
     */
    public int getTotalDocuments() {
        return index.getTotalDocuments();
    }
}