package io.beekeeper.bots.pizza

import io.beekeeper.bots.pizza.chatlistener.User

class OrderSession(val conversationId: Int) {

    private val orderItems = mutableMapOf<String, MutableList<OrderItem>>()

    var isConfirmationOngoing = false
    var confirmingUser: User? = null

    fun hasOrderItem(user: User): Boolean {
        return orderItems.containsKey(user.id)
    }

    fun addOrderItem(user: User, orderItem: OrderItem) {
        orderItems.getOrPut(user.id) { mutableListOf() }.add(orderItem)
    }

    fun getOrderItems(): Collection<OrderItem> = orderItems.values.flatten()

    fun removeOrderItems(user: User) {
        orderItems.remove(user.id)
    }

}
