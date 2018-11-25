package io.beekeeper.bots.pizza.dto

data class OrderItem(val user: User, val menuItem: MenuItem) {

    val itemName: String
        get() = menuItem.articleName

    val itemPrice: Float?
        get() = menuItem.price

}
