package io.beekeeper.bots.pizza;

import io.beekeeper.bots.pizza.crawler.DieciMenuItem;

public class OrderItem {

    OrderItem(String originalText, DieciMenuItem menuItem) {
        this.originalText = originalText;
        this.menuItem = menuItem;
    }

    private String originalText;
    private DieciMenuItem menuItem;

    public String getOriginalText() {
        return originalText;
    }

    public DieciMenuItem getMenuItem() {
        return menuItem;
    }

}
