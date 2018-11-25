package io.beekeeper.bots.pizza.chatlistener

data class Message(
        val text: String,
        val conversationId: Int,
        val sender: User
)