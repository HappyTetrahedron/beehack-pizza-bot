package io.beekeeper.bots.pizza;

import io.beekeeper.sdk.ChatBot;
import io.beekeeper.sdk.exception.BeekeeperException;
import io.beekeeper.sdk.model.ConversationMessage;

public class PizzaBot extends ChatBot {

    public PizzaBot(String tenantUrl, String apiToken) {
        super(tenantUrl, apiToken);
    }

    @Override
    public void onNewMessage(ConversationMessage message, ConversationHelper conversationHelper) {
        try {
            conversationHelper.reply(message.getText());
        } catch (BeekeeperException e) {
            e.printStackTrace();
        }
    }

}
