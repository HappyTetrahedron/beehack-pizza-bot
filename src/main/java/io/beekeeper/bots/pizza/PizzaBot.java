package io.beekeeper.bots.pizza;

import io.beekeeper.sdk.ChatBot;
import io.beekeeper.sdk.exception.BeekeeperException;
import io.beekeeper.sdk.model.Conversation;
import io.beekeeper.sdk.model.ConversationMessage;

public class PizzaBot extends ChatBot {

    private OrderSession orderSession = null;

    public PizzaBot(String tenantUrl, String apiToken) {
        super(tenantUrl, apiToken);
    }

    @Override
    public void onNewMessage(ConversationMessage message, ConversationHelper conversationHelper) {
        try {
            if (message.getText() == null) {
                return;
            }

            Conversation conversation = getSdk().getConversations().getConversationById(message.getConversationId()).execute();

            if (conversation.isGroupConversation()) {
                processGroupMessage(conversation, message, conversationHelper);
            } else {
                processOneOnOneMessage();
            }


        } catch (BeekeeperException e) {
            e.printStackTrace();
            try {
                conversationHelper.reply("Something went wrong... sorry");
            } catch (BeekeeperException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void processGroupMessage(Conversation conversation, ConversationMessage message, ConversationHelper conversationHelper) throws BeekeeperException {
        // Case 1: Order is started
        if (message.getText().equals("/start")) {
            startOrder(conversation, conversationHelper);
            return;
        }

        // Case 2: An item is added to the existing order

        // Case 3: Order is closed / sent
        if (message.getText().equals("/cancel")) {
            cancelOrder(conversation, conversationHelper);
            return;
        }


        // Case 5: Help commands / get list of available items

    }


    private void startOrder(Conversation conversation, ConversationHelper conversationHelper) throws BeekeeperException {
        if (orderSession != null) {
            conversationHelper.reply("There is already an ongoing order.");
            return;
        }

        orderSession = new OrderSession(conversation);
        conversationHelper.reply("Order started");
    }

    private void cancelOrder(Conversation conversation, ConversationHelper conversationHelper) throws BeekeeperException {
        if (orderSession == null || orderSession.getConversation().getId() != conversation.getId()) {
            conversationHelper.reply("There was no ongoing order.");
            return;
        }

        orderSession = null;
        conversationHelper.reply("Order cancelled.");
    }

    private void processOneOnOneMessage() {
        // Case 4: Correction / cancellation (1-on-1)
    }

}
