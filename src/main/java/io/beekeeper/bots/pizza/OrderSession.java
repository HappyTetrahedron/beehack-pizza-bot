package io.beekeeper.bots.pizza;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class OrderSession {

    private final int conversationId;

    private final HashMap<String, List<OrderItem>> orderItems = new HashMap<>();

    public OrderSession(int conversationId) {
        this.conversationId = conversationId;
    }

    public int getConversationId() {
        return conversationId;
    }

    public boolean hasOrderItem(String userId) {
        return orderItems.containsKey(userId);
    }

    public void addOrderItem(String userId, OrderItem orderItem) {
        if (!orderItems.containsKey(userId)) {
            orderItems.put(userId, new ArrayList<>());
        }
        orderItems.get(userId).add(orderItem);
    }

    public Collection<OrderItem> getOrderItems() {
        List<OrderItem> orders = new ArrayList<>();
        for (List<OrderItem> o : orderItems.values()) {
            orders.addAll(o);
        }
        return orders;
    }

    public void removeOrderItems(String userId) {
        orderItems.remove(userId);
    }
}
