package io.beekeeper.bots.pizza;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parser<T> {

    private final Map<String, T> availableItems;

    // Knobs to tweak parsing accuracy and strictness
    private static final int min_words = 1;
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
            for (int orderWordIdx = 0; orderWordIdx < orderParts.length; orderWordIdx++) {
                for (int itemNameWordIdx = 0; itemNameWordIdx < nameParts.length; itemNameWordIdx++) {

                    String prefix = commonPrefix(orderParts[orderWordIdx], nameParts[itemNameWordIdx]);
                    if (prefix.length() >= Math.min(min_chars_first_word, nameParts[itemNameWordIdx].length())) {
                        // Forward scanning mode engage!
                        List<String> matches = new ArrayList<>();
                        for (int nn = itemNameWordIdx; nn < nameParts.length; nn++) {
                            if (orderWordIdx - itemNameWordIdx + nn < orderParts.length) {
                                String nextPrefix = commonPrefix(nameParts[nn], orderParts[orderWordIdx - itemNameWordIdx + nn]);
                                if (nextPrefix.length() >= Math.min(min_chars_subsequent_words, nameParts[nn].length())) {
                                    matches.add(nextPrefix);
                                }
                            }
                        }
                        if (matches.size() >= Math.min(min_words, nameParts.length)) {
                            if (countCharsInStringList(matches) >= Math.min(min_chars_total, countCharsInStringList(nameParts))) {
                                matchesFound.add(new MatchData(matches.size(),
                                        countCharsInStringList(matches),
                                        itemNameWordIdx,
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
        return aList.stream().mapToInt(String::length).sum();
    }

    private int countCharsInStringList(String[] aList) {
        // Even though the code is equal Java won't let me write only one method.
        return Arrays.stream(aList).mapToInt(String::length).sum();
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
                .toLowerCase()
                .split(" ");
    }

    private class MatchData implements Comparable<MatchData> {
        int len;
        int sum;
        int index;
        String productName;

        MatchData(int len, int sum, int index, String productName) {
            this.len = len;
            this.sum = sum;
            this.index = index;
            this.productName = productName;
        }

        @Override
        public int compareTo(MatchData that) {
            int lens = Integer.compare(that.len, this.len);
            if (lens != 0) return lens;
            return Integer.compare(that.sum, this.sum);
        }
    }
}
