package io.beekeeper.bots.pizza.beekeeper

import io.beekeeper.bots.pizza.dto.Chat
import io.beekeeper.bots.pizza.dto.User
import io.beekeeper.bots.pizza.messenger.Messenger
import io.beekeeper.bots.pizza.messenger.MessengerException
import io.beekeeper.sdk.BeekeeperSDK
import io.beekeeper.sdk.exception.BeekeeperException
import io.beekeeper.sdk.params.SendMessageParams
import java.io.File

class BeekeeperMessenger(private val sdk: BeekeeperSDK) : Messenger {

    override fun sendMessage(chat: Chat, text: String) {
        try {
            sdk.conversations.sendMessage(chat.conversationId, text).execute()
        } catch (e: BeekeeperException) {
            throw MessengerException("Failed to send message to conversation $chat.conversationId", e)
        }
    }

    override fun sendEventMessage(chat: Chat, text: String) {
        try {
            sdk.conversations.sendEventMessage(chat.conversationId, text).execute()
        } catch (e: BeekeeperException) {
            throw MessengerException("Failed to send event message to conversation $chat.conversationId", e)
        }
    }

    override fun sendMessageToUser(user: User, text: String) {
        try {
            sdk.conversations.sendMessageToUser(user.username, text).execute();
        } catch (e: BeekeeperException) {
            throw MessengerException("Failed to send message to user ${user.username}", e)
        }
    }

    override fun sendImage(chat: Chat, image: File) {
        try {
            val file = sdk.files.uploadPhoto(image).execute()
            sdk.conversations.sendMessage(chat.conversationId, SendMessageParams.builder()
                    .photoId(file.id)
                    .build())
                    .execute()
        } catch (e: BeekeeperException) {
            throw MessengerException("Failed to send confirmation image", e)
        }
    }

}