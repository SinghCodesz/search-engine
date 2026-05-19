package com.searchengine.test;

import com.searchengine.text.TextProcessor;
import java.util.List;

public class TextProcessorTest {
    public static void main(String[] args) {
        TextProcessor processor = new TextProcessor();

        // Test 1: Basic sentence
        String text1 = "The quick brown foxes are jumping over the lazy dogs";
        List<String> tokens1 = processor.process(text1);

        System.out.println("Input: " + text1);
        System.out.println("Output: " + tokens1);
        System.out.println("Stats: " + processor.getStats(text1));
        System.out.println();

        // Test 2: Technical text
        String text2 = "Running applications in distributed systems requires careful synchronization";
        List<String> tokens2 = processor.process(text2);

        System.out.println("Input: " + text2);
        System.out.println("Output: " + tokens2);
        System.out.println("Stats: " + processor.getStats(text2));
        System.out.println();

        // Test 3: Process a crawled document
        String text3 = "GitHub is a development platform inspired by the way you work. " +
                "From open source to business, you can host and review code, " +
                "manage projects, and build software alongside millions of other developers.";
        List<String> tokens3 = processor.process(text3);

        System.out.println("Input: " + text3);
        System.out.println("Output: " + tokens3);
        System.out.println("Stats: " + processor.getStats(text3));

        System.out.println("\n✅ Text processing pipeline working!");
    }
}