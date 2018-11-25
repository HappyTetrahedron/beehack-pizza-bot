package io.beekeeper.bots.pizza.beekeeper

import io.beekeeper.bots.pizza.extensions.logger
import io.beekeeper.sdk.BeekeeperSDK
import io.beekeeper.sdk.exception.BeekeeperException

class GroupConversationManager(private val sdk: BeekeeperSDK) {

    private val cache = mutableMapOf<Int, Boolean>()

    fun isGroupConversation(conversationId: Int): Boolean? {
        try {
            // TODO: Make thread-safe
            var isGroup = cache[conversationId]
            if (isGroup == null) {
                log.info("Cache miss: group conversation with ID $conversationId not found")
                isGroup = sdk.conversations.getConversationById(conversationId).execute().isGroupConversation
                cache[conversationId] = isGroup
            }
            return isGroup
        } catch (e: BeekeeperException) {
            log.error("Failed to load conversation $conversationId into cache", e)
            return null
        }
    }

    companion object {
        val log = logger()
    }

}