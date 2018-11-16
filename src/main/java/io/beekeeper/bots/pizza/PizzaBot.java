package io.beekeeper.bots.pizza;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.beekeeper.sdk.ChatBot;
import io.beekeeper.sdk.exception.BeekeeperException;
import io.beekeeper.sdk.model.Conversation;
import io.beekeeper.sdk.model.ConversationMessage;

public class PizzaBot extends ChatBot {

    private final Pattern ITEM_ORDER_PATTERN = Pattern.compile("^/order\\s(.*)");

    private OrderSession orderSession = null;
    private Parser parser = null;

    public PizzaBot(String tenantUrl, String apiToken) {
        super(tenantUrl, apiToken);
    }

    public void setParser(Parser parser) {
        this.parser = parser;
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

        // Case 2: An item is added to the existing
        Matcher matcher = ITEM_ORDER_PATTERN.matcher(message.getText());
        if (matcher.matches()) {
            processItemAdding(conversation, message, matcher.group(1), conversationHelper);
            return;
        }

        // Case 3: Order is closed / sent
        if (message.getText().equals("/cancel")) {
            cancelOrder(conversation, conversationHelper);
            return;
        }

        // Case 5: Help commands / get list of available items
        if (message.getText().equals("/orders")) {
            showOrders(conversation, conversationHelper);
            return;
        }

        if (message.getText().equals("/help")) {
            showHelp(conversationHelper);
        }
    }

    private void showHelp(ConversationHelper conversationHelper) throws BeekeeperException {
        String helpText =
                "/help show this help\n" +
                        "/start start a new pizza order\n" +
                        "/cancel cancel the current pizza order\n" +
                        "/orders show the currently registerd orders\n" +
                        "/order [pizza] add a pizza with given name to the order\n";
        conversationHelper.reply(helpText);
    }

    private void showOrders(Conversation conversation, ConversationHelper conversationHelper) throws BeekeeperException {
        if (orderSession == null || orderSession.getConversation().getId() != conversation.getId()) {
            conversationHelper.reply("There is no ongoing order.");
            return;
        }


        Collection<OrderItem> orderItems = orderSession.getOrderItems();
        if (orderItems.isEmpty()) {
            conversationHelper.reply("Nothing was added to this order yet.");
            return;
        }
        StringBuilder builder = new StringBuilder("Current orders:\n");
        for (OrderItem orderItem : orderItems) {
            builder.append("\n- " + orderItem.getItemName());
        }
        conversationHelper.reply(builder.toString());
    }

    private void processItemAdding(Conversation conversation, ConversationMessage message, String itemName, ConversationHelper conversationHelper) throws BeekeeperException {
        if (orderSession == null || orderSession.getConversation().getId() != conversation.getId()) {
            conversationHelper.reply("There is no ongoing order.");
            return;
        }

        orderSession.updateOrderItem(message.getUserId(), new OrderItem(itemName));
        getSdk().getConversations().sendMessageToUser(message.getUsername(), "Your order for \"" + conversation.getName() + "\": " + itemName).execute();
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
