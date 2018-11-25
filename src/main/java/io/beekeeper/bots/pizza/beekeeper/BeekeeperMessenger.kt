package io.beekeeper.bots.pizza.beekeeper

import io.beekeeper.bots.pizza.messenger.Messenger
import io.beekeeper.bots.pizza.messenger.MessengerException
import io.beekeeper.sdk.BeekeeperSDK
import io.beekeeper.sdk.exception.BeekeeperException

class BeekeeperMessenger(private val sdk: BeekeeperSDK) : Messenger {

    override fun sendMessage(conversationId: Int, text: String) {
        try {
            sdk.conversations.sendMessage(conversationId, text).execute()
        } catch (e: BeekeeperException) {
            throw MessengerException("Failed to send message to conversation $conversationId", e)
        }
    }

    override fun sendEventMessage(conversationId: Int, text: String) {
        try {
            sdk.conversations.sendEventMessage(conversationId, text).execute()
        } catch (e: BeekeeperException) {
            throw MessengerException("Failed to send event message to conversation $conversationId", e)
        }
    }

    override fun sendMessageToUser(username: String, text: String) {
        try {
            sdk.conversations.sendMessageToUser(username, text).execute();
        } catch (e: BeekeeperException) {
            throw MessengerException("Failed to send message to user $username", e)
        }
    }

}