package io.beekeeper.bots.pizza

import io.beekeeper.bots.pizza.chatlistener.User
import io.beekeeper.bots.pizza.crawler.DieciMenuItem

data class OrderItem(val user: User, val menuItem: DieciMenuItem)
