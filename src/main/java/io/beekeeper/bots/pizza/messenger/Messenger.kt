package io.beekeeper.bots.pizza.messenger

interface Messenger {

    fun sendMessage(conversationId: Int, text: String)

    fun sendEventMessage(conversationId: Int, text: String)

    fun sendMessageToUser(username: String, text: String)

}