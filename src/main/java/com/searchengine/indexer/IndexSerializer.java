package com.searchengine.indexer;

import java.io.*;
import java.util.*;

public class IndexSerializer {

    private static final String INDEX_DIR = "data/index";
    private static final String TERMS_FILE = "terms.dict";
    private static final String POSTINGS_FILE = "postings.dat";
    private static final String DOCS_FILE = "docs.map";

    public void saveIndex(InvertedIndex index, Map<Integer, String[]> docMap) {
        File dir = new File(INDEX_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            saveTerms(index);
            savePostings(index);
            saveDocumentMap(docMap);
            System.out.println("Index saved to " + INDEX_DIR);
        } catch (IOException e) {
            System.err.println("Failed to save index: " + e.getMessage());
        }
    }

    private void saveTerms(InvertedIndex index) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(INDEX_DIR + "/" + TERMS_FILE))) {
            int offset = 0;
            for (String term : index.getAllTerms()) {
                List<Posting> postings = index.getPostings(term);
                int postingSize = calculatePostingSize(postings);
                writer.println(term + "," + offset + "," + postingSize + "," + postings.size());
                offset += postingSize;
            }
        }
    }

    private void savePostings(InvertedIndex index) throws IOException {
        try (DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(
                        new FileOutputStream(INDEX_DIR + "/" + POSTINGS_FILE)))) {

            for (String term : index.getAllTerms()) {
                List<Posting> postings = index.getPostings(term);
                out.writeInt(postings.size());

                for (Posting p : postings) {
                    out.writeInt(p.getDocumentId());
                    out.writeInt(p.getTermFrequency());

                    List<Integer> positions = p.getPositions();
                    out.writeInt(positions.size());
                    for (int pos : positions) {
                        out.writeInt(pos);
                    }
                }
            }
        }
    }

    private void saveDocumentMap(Map<Integer, String[]> docMap) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(INDEX_DIR + "/" + DOCS_FILE))) {
            for (Map.Entry<Integer, String[]> entry : docMap.entrySet()) {
                int docId = entry.getKey();
                String[] info = entry.getValue();
                writer.println(docId + "|" + info[0] + "|" + info[1]);
            }
        }
    }

    public InvertedIndex loadIndex() {
        File termsFile = new File(INDEX_DIR + "/" + TERMS_FILE);
        File postingsFile = new File(INDEX_DIR + "/" + POSTINGS_FILE);

        if (!termsFile.exists() || !postingsFile.exists()) {
            System.out.println("No saved index found. Building from scratch.");
            return new InvertedIndex();
        }

        try {
            InvertedIndex index = new InvertedIndex();
            Map<String, long[]> termMetadata = loadTermDictionary();
            loadPostingsIntoIndex(index, termMetadata);

            // Count total documents from document map
            Map<Integer, String[]> docMap = loadDocumentMap();
            index.setTotalDocuments(docMap.size());

            System.out.println("Index loaded from " + INDEX_DIR);
            return index;
        } catch (IOException e) {
            System.err.println("Failed to load index: " + e.getMessage());
            return new InvertedIndex();
        }
    }

    private Map<String, long[]> loadTermDictionary() throws IOException {
        Map<String, long[]> termMetadata = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(
                new FileReader(INDEX_DIR + "/" + TERMS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                String term = parts[0];
                long offset = Long.parseLong(parts[1]);
                long size = Long.parseLong(parts[2]);
                termMetadata.put(term, new long[]{offset, size});
            }
        }

        return termMetadata;
    }

    private void loadPostingsIntoIndex(InvertedIndex index,
                                       Map<String, long[]> termMetadata)
            throws IOException {
        try (DataInputStream in = new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream(INDEX_DIR + "/" + POSTINGS_FILE)))) {

            for (Map.Entry<String, long[]> entry : termMetadata.entrySet()) {
                String term = entry.getKey();

                int numPostings = in.readInt();

                for (int i = 0; i < numPostings; i++) {
                    int docId = in.readInt();
                    int freq = in.readInt();
                    int numPositions = in.readInt();

                    List<Integer> positions = new ArrayList<>();
                    for (int j = 0; j < numPositions; j++) {
                        positions.add(in.readInt());
                    }

                    index.addDocumentDirect(term, docId, freq, positions);
                }
            }
        }
    }

    public Map<Integer, String[]> loadDocumentMap() {
        Map<Integer, String[]> docMap = new HashMap<>();
        File docsFile = new File(INDEX_DIR + "/" + DOCS_FILE);

        if (!docsFile.exists()) {
            return docMap;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(docsFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|", 3);
                int docId = Integer.parseInt(parts[0]);
                String url = parts[1];
                String title = parts.length > 2 ? parts[2] : "Untitled";
                docMap.put(docId, new String[]{url, title});
            }
        } catch (IOException e) {
            System.err.println("Failed to load document map: " + e.getMessage());
        }

        return docMap;
    }

    private int calculatePostingSize(List<Posting> postings) {
        int size = 4;
        for (Posting p : postings) {
            size += 4;
            size += 4;
            size += 4;
            size += p.getPositions().size() * 4;
        }
        return size;
    }
}