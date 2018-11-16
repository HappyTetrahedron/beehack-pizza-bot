package io.beekeeper.bots.pizza;

import io.beekeeper.bots.pizza.crawler.DieciMenuItem;

public class OrderItem {

    OrderItem(String ordererDisplayName, String originalText, DieciMenuItem menuItem) {
        this.ordererDisplayName = ordererDisplayName;
        this.originalText = originalText;
        this.menuItem = menuItem;
    }

    private final String originalText;
    private final String ordererDisplayName;
    private final DieciMenuItem menuItem;

    public String getOriginalText() {
        return originalText;
    }

    public String getOrdererDisplayName() {
        return ordererDisplayName;
    }

    public DieciMenuItem getMenuItem() {
        return menuItem;
    }

}
