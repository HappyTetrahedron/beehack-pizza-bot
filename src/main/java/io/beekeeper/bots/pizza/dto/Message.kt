package io.beekeeper.bots.pizza.dto

data class Message(
        val text: String,
        val chat: Chat,
        val sender: User
)