package io.beekeeper.bots.pizza.parser

import io.beekeeper.bots.pizza.dto.MenuItem


interface MenuItemParser<T : MenuItem> {

    fun parse(text: String): T?

}