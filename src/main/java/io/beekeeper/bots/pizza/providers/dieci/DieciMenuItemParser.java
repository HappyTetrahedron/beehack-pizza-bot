package io.beekeeper.bots.pizza.providers.dieci;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import io.beekeeper.bots.pizza.dto.MenuItem;
import io.beekeeper.bots.pizza.parser.MenuItemParser;

public class DieciMenuItemParser<T extends MenuItem> implements MenuItemParser<T> {

    private final Map<String, T> availableItems;

    // Knobs to tweak parsing accuracy and strictness
    private static final int MIN_WORDS = 1;
    private static final int MIN_CHARS_FIRST_WORD = 3;
    private static final int MIN_CHARS_SUBSEQUENT_WORDS = 2;
    private static final int MIN_CHARS_TOTAL = 5;

    public DieciMenuItemParser(Map<String, T> items) {
        this.availableItems = items;
    }

    @Override
    public T parse(@NotNull String message) {
        List<MatchData> matchesFound = new ArrayList<>();
        String[] orderParts = normalizeAndSplit(message);
        for (String itemName : availableItems.keySet()) {
            String[] nameParts = normalizeAndSplit(itemName);
            for (int orderWordIdx = 0; orderWordIdx < orderParts.length; orderWordIdx++) {
                for (int itemNameWordIdx = 0; itemNameWordIdx < nameParts.length; itemNameWordIdx++) {

                    String prefix = commonPrefix(orderParts[orderWordIdx], nameParts[itemNameWordIdx]);
                    if (prefix.length() >= Math.min(MIN_CHARS_FIRST_WORD, nameParts[itemNameWordIdx].length())) {
                        // Forward scanning mode engage!
                        List<String> matches = new ArrayList<>();
                        for (int nn = itemNameWordIdx; nn < nameParts.length; nn++) {
                            if (orderWordIdx - itemNameWordIdx + nn < orderParts.length) {
                                String nextPrefix = commonPrefix(nameParts[nn], orderParts[orderWordIdx - itemNameWordIdx + nn]);
                                if (nextPrefix.length() >= Math.min(MIN_CHARS_SUBSEQUENT_WORDS, nameParts[nn].length())) {
                                    matches.add(nextPrefix);
                                }
                            }
                        }
                        if (matches.size() >= Math.min(MIN_WORDS, nameParts.length)) {
                            if (countCharsInStringList(matches) >= Math.min(MIN_CHARS_TOTAL, countCharsInStringList(nameParts))) {
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
            int sums = Integer.compare(that.sum, this.sum);
            if (sums != 0) return sums;
            int idx = Integer.compare(this.index, that.index);
            if (idx != 0) return idx;
            int thisWordCount = this.productName.trim().split(" ").length;
            int thatWordCount = that.productName.trim().split(" ").length;
            int wordCounts = Integer.compare(thisWordCount, thatWordCount);
            if (wordCounts != 0) return wordCounts;
            if (this.productName.contains("normal") && !that.productName.contains("normal")) return -1;
            else if (that.productName.contains("normal") && !this.productName.contains("normal")) return 1;
            if (this.productName.contains("regular") && !that.productName.contains("regular")) return -1;
            else if (that.productName.contains("regular") && !this.productName.contains("regular")) return 1;
            return 0;
        }
    }
}
