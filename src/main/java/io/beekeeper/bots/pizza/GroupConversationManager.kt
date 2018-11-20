package io.beekeeper.bots.pizza

import io.beekeeper.sdk.BeekeeperSDK

class GroupConversationManager(private val sdk: BeekeeperSDK) {

    private val cache = mutableMapOf<Int, Boolean>()

    fun isGroupConversation(conversationId: Int): Boolean {
        // TODO: Make thread-safe
        var isGroup = cache.get(conversationId)
        if (isGroup == null) {
            println("Cache miss: group conversation with ID $conversationId not found")
            isGroup = sdk.conversations.getConversationById(conversationId).execute().isGroupConversation
            cache[conversationId] = isGroup
        }
        return isGroup
    }

}