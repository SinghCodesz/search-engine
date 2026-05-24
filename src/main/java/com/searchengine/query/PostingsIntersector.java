package com.searchengine.query;

import java.util.*;

/**
 * Intersects (AND) or merges (OR) posting lists.
 * Uses two-pointer merge algorithm for O(n+m) performance.
 */
public class PostingsIntersector {

    /**
     * AND intersection: find documents containing ALL terms.
     * Uses two-pointer algorithm on sorted posting lists.
     */
    public List<Integer> intersect(List<List<Integer>> postingLists) {
        if (postingLists == null || postingLists.isEmpty()) {
            return new ArrayList<>();
        }

        // Start with the smallest list for efficiency
        postingLists.sort(Comparator.comparingInt(List::size));

        // Start with the first (smallest) list
        List<Integer> result = new ArrayList<>(postingLists.get(0));

        // Intersect with each subsequent list
        for (int i = 1; i < postingLists.size(); i++) {
            result = intersectTwoLists(result, postingLists.get(i));
            if (result.isEmpty()) {
                return result; // Early exit if no common documents
            }
        }

        return result;
    }

    /**
     * Intersect two sorted lists using two-pointer merge.
     * O(n + m) where n and m are list sizes.
     */
    private List<Integer> intersectTwoLists(List<Integer> list1, List<Integer> list2) {
        List<Integer> result = new ArrayList<>();
        int i = 0, j = 0;

        while (i < list1.size() && j < list2.size()) {
            int doc1 = list1.get(i);
            int doc2 = list2.get(j);

            if (doc1 == doc2) {
                result.add(doc1);
                i++;
                j++;
            } else if (doc1 < doc2) {
                i++;
            } else {
                j++;
            }
        }

        return result;
    }

    /**
     * OR union: find documents containing ANY term.
     */
    public List<Integer> union(List<List<Integer>> postingLists) {
        Set<Integer> unionSet = new HashSet<>();
        for (List<Integer> list : postingLists) {
            unionSet.addAll(list);
        }
        List<Integer> result = new ArrayList<>(unionSet);
        Collections.sort(result);
        return result;
    }
}