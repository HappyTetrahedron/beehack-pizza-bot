package io.beekeeper.bots.pizza;

public class OrderItem {

    OrderItem(String itemName) {
        this.itemName = itemName;
    }

    private String itemName;

    public String getItemName() {
        return itemName;
    }
}
