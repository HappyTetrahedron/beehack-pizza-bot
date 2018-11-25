package io.beekeeper.bots.pizza

import io.beekeeper.bots.pizza.dto.Chat
import io.beekeeper.bots.pizza.dto.OrderItem
import io.beekeeper.bots.pizza.dto.User

class OrderSession(val chat: Chat) {

    private val orderItems = mutableMapOf<String, MutableList<OrderItem>>()

    var state: OrderState = OrderState.OPEN

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

    enum class OrderState {

        OPEN,
        SUBMITTED,
        CONFIRMED

    }

}
