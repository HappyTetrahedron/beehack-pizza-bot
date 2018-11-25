package io.beekeeper.bots.pizza.messenger

import io.beekeeper.bots.pizza.dto.Chat
import io.beekeeper.bots.pizza.dto.User

interface Messenger {

    fun sendMessage(chat: Chat, text: String)

    fun sendEventMessage(chat: Chat, text: String)

    fun sendMessageToUser(user: User, text: String)

}