package io.beekeeper.bots.pizza.test

import io.beekeeper.bots.pizza.dto.MenuItem

class TestMenuItem(override val articleName: String) : MenuItem {

    override val price: Float?
        get() = null

}