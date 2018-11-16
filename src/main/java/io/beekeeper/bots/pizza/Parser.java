package io.beekeeper.bots.pizza;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parser<T> {

    private Map<String, T> availableItems;

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
        return null;
    }
}
