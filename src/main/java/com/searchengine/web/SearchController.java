package com.searchengine.web;

import com.searchengine.indexer.InvertedIndex;
import com.searchengine.query.SearchService;
import com.searchengine.ranker.DocumentScore;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.searchengine.query.Trie;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.stream.Collectors;

import java.util.*;

@Controller
public class SearchController {

    private final SearchService searchService;
    private final Map<Integer, String[]> docMap;
    private final InvertedIndex index;
    private final Trie trie;

    public SearchController(SearchService searchService,
                            Map<Integer, String[]> docMap,
                            InvertedIndex index, Trie trie) {
        this.searchService = searchService;
        this.docMap = docMap;
        this.index = index;
        this.trie = trie;
    }

    @GetMapping("/")
    public String homePage() {
        return "search";
    }

    @GetMapping("/search")
    public String search(@RequestParam(defaultValue = "") String q,
                         @RequestParam(defaultValue = "0") int page,
                         Model model) {

        model.addAttribute("query", q);

        if (q.trim().isEmpty()) {
            model.addAttribute("results", new ArrayList<>());
            return "search";
        }

        // Search
        List<DocumentScore> allResults = searchService.search(q);

        // Spell correction
        String correction = searchService.getSpellCorrection(q);
        if (correction != null && !correction.equals(q)) {
            model.addAttribute("correction", correction);
        }

        // Pagination: 10 results per page
        int pageSize = 10;
        int totalResults = allResults.size();
        int totalPages = (int) Math.ceil((double) totalResults / pageSize);

        int fromIndex = page * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalResults);

        List<DocumentScore> pageResults = new ArrayList<>();
        if (fromIndex < totalResults) {
            pageResults = allResults.subList(fromIndex, toIndex);
        }

        // Enrich results with document info and snippets
        List<Map<String, Object>> enrichedResults = new ArrayList<>();
        for (DocumentScore ds : pageResults) {
            Map<String, Object> result = new HashMap<>();
            String[] docInfo = docMap.get(ds.getDocumentId());
            result.put("title", docInfo != null ? docInfo[1] : "Untitled");
            result.put("url", docInfo != null ? docInfo[0] : "#");
            result.put("score", ds.getScore());
            result.put("snippet", generateSnippet(docInfo));
            enrichedResults.add(result);
        }

        model.addAttribute("results", enrichedResults);
        model.addAttribute("totalResults", totalResults);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("hasNext", page < totalPages - 1);
        model.addAttribute("hasPrev", page > 0);

        return "search";
    }

    @GetMapping("/api/autocomplete")
    @ResponseBody
    public List<Map<String, Object>> autocomplete(@RequestParam String q) {
        if (q == null || q.trim().length() < 2) {
            return new ArrayList<>();
        }

        List<String> suggestions = trie.getSuggestions(q.trim().toLowerCase());

        return suggestions.stream()
                .map(word -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("word", word);
                    item.put("frequency", trie.getFrequency(word));
                    return item;
                })
                .collect(Collectors.toList());
    }

    private String generateSnippet(String[] docInfo) {
        if (docInfo == null) return "";
        String text = docInfo[1] + " — " + docInfo[0];
        if (text.length() > 200) {
            text = text.substring(0, 197) + "...";
        }
        return text;
    }
}