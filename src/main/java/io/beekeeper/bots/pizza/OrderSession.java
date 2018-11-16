package io.beekeeper.bots.pizza;

import io.beekeeper.sdk.model.Conversation;

public class OrderSession {

    private final Conversation conversation;

    public OrderSession(Conversation conversation) {
        this.conversation = conversation;
    }

    public Conversation getConversation() {
        return conversation;
    }

}
