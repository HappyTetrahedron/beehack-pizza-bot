package io.beekeeper.bots.pizza.beekeeper

import io.beekeeper.bots.pizza.dto.Chat
import io.beekeeper.bots.pizza.dto.Message
import io.beekeeper.bots.pizza.dto.User
import io.beekeeper.sdk.BeekeeperSDK
import io.beekeeper.sdk.ChatBot
import io.beekeeper.sdk.model.ConversationMessage

class BeekeeperChatListener(private val sdk: BeekeeperSDK) {

    private val subscribers = mutableListOf<(Message) -> Unit>()

    private val chatBot = object : ChatBot(sdk) {
        override fun onNewMessage(message: ConversationMessage, conversationHelper: ConversationHelper) {
            val messageDTO = Message(
                    sender = User(
                            id = message.userId,
                            username = message.username,
                            displayName = message.displayName
                    ),
                    text = message.text ?: "",
                    chat = Chat(
                            conversationId = message.conversationId
                    )
            )
            notifySubscribers(messageDTO)
        }
    }

    fun start() {
        chatBot.start()
    }

    fun register(subscriber: (Message) -> Unit) {
        subscribers.add(subscriber)
    }

    private fun notifySubscribers(message: Message) {
        subscribers.forEach { it.invoke(message) }
    }

}