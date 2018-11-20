package io.beekeeper.bots.pizza

class OrderSession(val conversationId: Int) {

    private val orderItems = mutableMapOf<String, MutableList<OrderItem>>()

    fun hasOrderItem(userId: String): Boolean {
        return orderItems.containsKey(userId)
    }

    fun addOrderItem(userId: String, orderItem: OrderItem) {
        orderItems.getOrPut(userId) { mutableListOf() }.add(orderItem)
    }

    fun getOrderItems(): Collection<OrderItem> = orderItems.values.flatten()

    fun removeOrderItems(userId: String) {
        orderItems.remove(userId)
    }

}
