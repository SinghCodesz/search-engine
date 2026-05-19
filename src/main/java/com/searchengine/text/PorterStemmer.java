package com.searchengine.text;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of Porter Stemming Algorithm.
 * Reduces words to their root form.
 *
 * Examples:
 *   "running"   → "run"
 *   "fishing"   → "fish"
 *   "happiness" → "happi"
 *   "cats"      → "cat"
 */
public class PorterStemmer {

    /**
     * Stem a single word to its root form.
     */
    public String stem(String word) {
        if (word == null || word.length() <= 2) {
            return word;
        }

        word = word.toLowerCase();

        // Step 1a: Handle plurals and past participles
        // SSES → SS (caresses → caress)
        if (word.endsWith("sses")) {
            word = word.substring(0, word.length() - 2);
        }
        // IES → I (ponies → poni)
        else if (word.endsWith("ies")) {
            word = word.substring(0, word.length() - 2);
        }
        // SS → SS (no change, caress → caress)
        // S → "" (cats → cat)
        else if (word.endsWith("s") && !word.endsWith("ss")) {
            word = word.substring(0, word.length() - 1);
        }

        // Step 1b: Handle -ed and -ing
        if (word.endsWith("eed")) {
            // agreed → agree
            word = word.substring(0, word.length() - 1);
        } else if (word.endsWith("ed") && word.length() > 3) {
            // worked → work
            word = word.substring(0, word.length() - 2);
        } else if (word.endsWith("ing") && word.length() > 4) {
            // running → runn (then handle double letter)
            word = word.substring(0, word.length() - 3);
            // Fix double letters: runn → run
            if (word.length() > 2 &&
                    word.charAt(word.length() - 1) == word.charAt(word.length() - 2)) {
                word = word.substring(0, word.length() - 1);
            }
        }

        // Step 2: Handle -ational, -tional, -ization, etc.
        if (word.endsWith("ational")) {
            word = word.substring(0, word.length() - 5) + "ate";
        } else if (word.endsWith("tional")) {
            word = word.substring(0, word.length() - 2);
        } else if (word.endsWith("ization")) {
            word = word.substring(0, word.length() - 5) + "ize";
        } else if (word.endsWith("fulness")) {
            word = word.substring(0, word.length() - 4);
        } else if (word.endsWith("ousness")) {
            word = word.substring(0, word.length() - 4);
        } else if (word.endsWith("alism")) {
            word = word.substring(0, word.length() - 3);
        } else if (word.endsWith("iveness")) {
            word = word.substring(0, word.length() - 4);
        } else if (word.endsWith("fulness")) {
            word = word.substring(0, word.length() - 4);
        }

        // Step 3: Handle -icate, -ative, -alize, -ical, -ful, -ness
        if (word.endsWith("icate")) {
            word = word.substring(0, word.length() - 3);
        } else if (word.endsWith("ative")) {
            word = word.substring(0, word.length() - 5);
        } else if (word.endsWith("alize")) {
            word = word.substring(0, word.length() - 3);
        } else if (word.endsWith("ical")) {
            word = word.substring(0, word.length() - 2);
        } else if (word.endsWith("ful")) {
            word = word.substring(0, word.length() - 3);
        } else if (word.endsWith("ness")) {
            word = word.substring(0, word.length() - 4);
        }

        // Step 4: Handle -al, -ance, -ence, -er, -ic, -able, -ible, -ant, -ement, -ment
        if (word.endsWith("al")) {
            word = word.substring(0, word.length() - 2);
        } else if (word.endsWith("ance")) {
            word = word.substring(0, word.length() - 4);
        } else if (word.endsWith("ence")) {
            word = word.substring(0, word.length() - 4);
        } else if (word.endsWith("er") && word.length() > 3) {
            word = word.substring(0, word.length() - 2);
        } else if (word.endsWith("ic")) {
            word = word.substring(0, word.length() - 2);
        } else if (word.endsWith("able")) {
            word = word.substring(0, word.length() - 4);
        } else if (word.endsWith("ible")) {
            word = word.substring(0, word.length() - 4);
        } else if (word.endsWith("ant")) {
            word = word.substring(0, word.length() - 3);
        } else if (word.endsWith("ement")) {
            word = word.substring(0, word.length() - 5);
        } else if (word.endsWith("ment")) {
            word = word.substring(0, word.length() - 4);
        } else if (word.endsWith("ent")) {
            word = word.substring(0, word.length() - 3);
        } else if (word.endsWith("ion")) {
            word = word.substring(0, word.length() - 3);
        } else if (word.endsWith("ou")) {
            word = word.substring(0, word.length() - 2);
        } else if (word.endsWith("ism")) {
            word = word.substring(0, word.length() - 3);
        } else if (word.endsWith("ate")) {
            word = word.substring(0, word.length() - 3);
        } else if (word.endsWith("iti")) {
            word = word.substring(0, word.length() - 3);
        } else if (word.endsWith("ous")) {
            word = word.substring(0, word.length() - 3);
        } else if (word.endsWith("ive")) {
            word = word.substring(0, word.length() - 3);
        } else if (word.endsWith("ize")) {
            word = word.substring(0, word.length() - 3);
        }

        // Handle 'e' at end
        if (word.endsWith("e") && word.length() > 3) {
            word = word.substring(0, word.length() - 1);
        }

        // Handle double letters at end
        if (word.length() > 2 &&
                word.charAt(word.length() - 1) == word.charAt(word.length() - 2) &&
                word.charAt(word.length() - 1) != 'a' &&
                word.charAt(word.length() - 1) != 'e' &&
                word.charAt(word.length() - 1) != 'i' &&
                word.charAt(word.length() - 1) != 'o' &&
                word.charAt(word.length() - 1) != 'u') {
            word = word.substring(0, word.length() - 1);
        }

        return word;
    }

    /**
     * Stem all tokens in a list.
     */
    public List<String> stemTokens(List<String> tokens) {
        List<String> stemmed = new ArrayList<>();
        for (String token : tokens) {
            stemmed.add(stem(token));
        }
        return stemmed;
    }
}