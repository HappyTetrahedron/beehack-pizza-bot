package io.beekeeper.bots.pizza.extensions

fun runAsync(block: () -> Unit) {
    Thread(block).start()
}