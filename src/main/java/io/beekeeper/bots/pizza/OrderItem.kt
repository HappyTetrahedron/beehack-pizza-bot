package io.beekeeper.bots.pizza

import io.beekeeper.bots.pizza.crawler.DieciMenuItem

data class OrderItem(val ordererDisplayName: String, val menuItem: DieciMenuItem)
