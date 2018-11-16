package io.beekeeper.bots.pizza;

import java.util.Collection;
import java.util.HashMap;

import io.beekeeper.sdk.model.Conversation;

public class OrderSession {

    private final Conversation conversation;

    private final HashMap<String, OrderItem> orderItems = new HashMap<>();

    public OrderSession(Conversation conversation) {
        this.conversation = conversation;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public boolean hasOrderItem(String userId) {
        return orderItems.containsKey(userId);
    }

    public void updateOrderItem(String userId, OrderItem orderItem) {
        if (orderItem == null) {
            orderItems.remove(userId);
        } else {
            orderItems.put(userId, orderItem);
        }
    }

    public Collection<OrderItem> getOrderItems() {
        return orderItems.values();
    }

}
