package io.beekeeper.bots.pizza;

import lombok.AllArgsConstructor;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parser<T> {

    private final Map<String, T> availableItems;

    // Knobs to tweak parsing accuracy and strictness
    private static final int min_words = 2;
    private static final int min_chars_first_word = 3;
    private static final int min_chars_subsequent_words = 2;
    private static final int min_chars_total = 5;

    public Parser(Map<String, T> items) {
        this.availableItems = items;
    }

    public Parser(List<String> names, List<T> items) {
        this.availableItems = new HashMap<>();
        for (int i = 0; i < Math.min(names.size(), items.size()); i++) {
            availableItems.put(names.get(i), items.get(i));
        }
    }

    public T parse(String message) {
        List<MatchData> matchesFound = new ArrayList<>();
        String[] orderParts = normalizeAndSplit(message);
        for (String itemName : availableItems.keySet()) {
            String[] nameParts = normalizeAndSplit(itemName);
            for (int o = 0; o < orderParts.length; o++) {
                for (int n = 0; n < nameParts.length; n++) {

                    String prefix = commonPrefix(orderParts[0], nameParts[n]);
                    if (prefix.length() >= Math.min(min_chars_first_word, nameParts[n].length())) {
                        // Forward scanning mode engage!
                        List<String> matches = new ArrayList<>();
                        for (int nn = n; nn < nameParts.length; nn++) {
                            if (nn < orderParts.length) {
                                String nextPrefix = commonPrefix(nameParts[nn], orderParts[o - n + nn]);
                                if (nextPrefix.length() >= Math.min(min_chars_subsequent_words, nameParts[nn].length())) {
                                    matches.add(nextPrefix);
                                }
                            }
                        }
                        if (matches.size() >= Math.min(min_words, nameParts.length)) {
                            if (countCharsInStringList(matches) >= Math.min(min_chars_total, countCharsInStringList(nameParts))) {
                                matchesFound.add(new MatchData(matches.size(),
                                        countCharsInStringList(matches),
                                        n,
                                        itemName));
                            }
                        }
                    }
                }
            }
        }
        if (matchesFound.size() == 0) return null;
        matchesFound.sort(MatchData::compareTo);
        return availableItems.get(matchesFound.get(0).productName);
    }

    private int countCharsInStringList(List<String> aList) {
        int sum = 0;
        for (String s : aList) {
            sum += s.length();
        }
        return sum;
    }

    private int countCharsInStringList(String[] aList) {
        // Even though the code is equal Java won't let me write only one method.
        int sum = 0;
        for (String s : aList) {
            sum += s.length();
        }
        return sum;
    }

    private String commonPrefix(String a, String b) {
        String res = "";
        for (int i = 0; i < Math.min(a.length(), b.length()); i++) {
            if (a.charAt(i) == b.charAt(i)) {
                res = res + a.charAt(i);
            } else break;
        }
        return res;
    }

    private String[] normalizeAndSplit(String original) {
        // Remove all umlauts/accents and split into words
        return Normalizer.normalize(original, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .trim()
                .split("");
    }

    @AllArgsConstructor
    private class MatchData implements Comparable<MatchData> {
        int len;
        int sum;
        int index;
        String productName;

        @Override
        public int compareTo(MatchData that) {
            int lens = Integer.compare(this.len, that.len);
            if (lens != 0) return lens;
            return Integer.compare(this.sum, that.sum);
        }
    }
}
