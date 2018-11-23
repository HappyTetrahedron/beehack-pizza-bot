package io.beekeeper.bots.pizza

import io.beekeeper.sdk.BeekeeperSDK
import io.beekeeper.sdk.exception.BeekeeperException

class Messenger(private val sdk: BeekeeperSDK) {

    @Throws(BeekeeperException::class)
    fun sendMessage(conversationId: Int, text: String) {
        sdk.conversations.sendMessage(conversationId, text).execute()
    }

    @Throws(BeekeeperException::class)
    fun sendConfirmationMessage(conversationId: Int, text: String) {
        sdk.conversations.sendEventMessage(conversationId, text).execute()
    }

    @Throws(BeekeeperException::class)
    fun sendMessageToUser(username: String, text: String) {
        sdk.conversations.sendMessageToUser(username, text).execute();
    }

}