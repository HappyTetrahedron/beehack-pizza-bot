package io.beekeeper.bots.pizza

import io.beekeeper.bots.pizza.dto.Chat

class OrderSessionManager {

    private val sessions = mutableMapOf<Int, OrderSession>()

    fun getSession(chat: Chat): OrderSession? = sessions[chat.conversationId]

    fun createSession(chat: Chat) {
        sessions[chat.conversationId] = OrderSession(chat)
    }

    fun deleteSession(session: OrderSession) {
        // TODO: Retain session for a while to allow restoring
        sessions.remove(session.chat.conversationId)
    }

}