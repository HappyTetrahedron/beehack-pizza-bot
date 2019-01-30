package io.beekeeper.bots.pizza.messenger

import io.beekeeper.bots.pizza.dto.Chat
import io.beekeeper.bots.pizza.dto.User
import java.io.File

interface Messenger {

    fun sendMessage(chat: Chat, text: String)

    fun sendEventMessage(chat: Chat, text: String)

    fun sendMessageToUser(user: User, text: String)

    fun sendImage(chat: Chat, image: File)

}