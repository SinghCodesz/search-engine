package com.searchengine;

import com.searchengine.indexer.IndexSerializer;
import com.searchengine.indexer.InvertedIndex;
import com.searchengine.query.SearchService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import com.searchengine.query.Trie;

import java.util.Map;

@SpringBootApplication
public class SearchEngineApplication {

    private static InvertedIndex index;
    private static Map<Integer, String[]> docMap;
    private static SearchService searchService;

    public static void main(String[] args) {
        // Load index on startup
        IndexSerializer serializer = new IndexSerializer();
        index = serializer.loadIndex();
        docMap = serializer.loadDocumentMap();

        if (index.getTotalDocuments() == 0) {
            System.out.println("No index found. Run the crawler first.");
        } else {
            System.out.println("Index loaded: " + index.getTotalDocuments()
                    + " documents, " + index.getVocabularySize() + " terms");
        }

        searchService = new SearchService(index);

        SpringApplication.run(SearchEngineApplication.class, args);
    }

    @Bean
    public InvertedIndex invertedIndex() {
        return index;
    }

    @Bean
    public Map<Integer, String[]> documentMap() {
        return docMap;
    }

    @Bean
    public SearchService searchService() {
        return searchService;
    }

    // Add this bean method
    @Bean
    public Trie trie() {
        Trie trie = new Trie();
        if (index != null) {
            System.out.println("Building autocomplete trie...");
            for (String term : index.getAllTerms()) {
                int frequency = index.getDocumentFrequency(term);
                trie.insert(term, frequency);
            }
            System.out.println("Trie built: " + trie.size() + " words");
        }
        return trie;
    }
}