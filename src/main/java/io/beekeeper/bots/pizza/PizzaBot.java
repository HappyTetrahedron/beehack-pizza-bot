package io.beekeeper.bots.pizza;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.beekeeper.bots.pizza.crawler.DieciMenuItem;
import io.beekeeper.sdk.ChatBot;
import io.beekeeper.sdk.exception.BeekeeperException;
import io.beekeeper.sdk.model.Conversation;
import io.beekeeper.sdk.model.ConversationMessage;


public class PizzaBot extends ChatBot {

    private final Pattern ITEM_ORDER_PATTERN = Pattern.compile("^/order\\s(.*)");

    public OrderSession getOrderSession() {
        return orderSession;
    }

    private OrderSession orderSession = null;
    private Parser<DieciMenuItem> parser = null;

    public PizzaBot(String tenantUrl, String apiToken) {
        super(tenantUrl, apiToken);
    }

    public void setParser(Parser<DieciMenuItem> parser) {
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

    void processGroupMessage(Conversation conversation, ConversationMessage message, ConversationHelper conversationHelper) throws BeekeeperException {
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

        if (message.getText().equals("/remove")) {
            processRemovingItem(conversation, message, conversationHelper);
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

    private void processRemovingItem(Conversation conversation, ConversationMessage message, ConversationHelper conversationHelper) throws BeekeeperException {
        if (orderSession == null || orderSession.getConversation().getId() != conversation.getId()) {
            conversationHelper.reply("There is no ongoing order.");
            return;
        }

        orderSession.updateOrderItem(message.getUserId(), null);
        String text = "Removed order for " + message.getDisplayName() + ".";
        sendConfirmationMessage(conversation, message, text);

    }

    private void showHelp(ConversationHelper conversationHelper) throws BeekeeperException {
        String helpText =
                "/help show this help\n" +
                        "/start start a new pizza order\n" +
                        "/cancel cancel the current pizza order\n" +
                        "/orders show the currently registered orders\n" +
                        "/order [pizza] add a pizza with given name to the order\n" +
                        "/remove remove your order";
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
        float total = 0;
        for (OrderItem orderItem : orderItems) {
            builder
                    .append("\n- ")
                    .append(orderItem.getOrdererDisplayName())
                    .append(": ")
                    .append(orderItem.getMenuItem().getArticleName())
                    .append(" (")
                    .append(formatPrice(orderItem.getMenuItem().getPrice()))
                    .append(")");

            total += orderItem.getMenuItem().getPrice();
        }
        builder.append("\n")
                .append("\n")
                .append("Total: ")
                .append(formatPrice(total));

        conversationHelper.reply(builder.toString());
    }

    private String formatPrice(float price) {
        return String.format("%.2f", price);
    }

    private void processItemAdding(Conversation conversation, ConversationMessage message, String itemName, ConversationHelper conversationHelper) throws BeekeeperException {
        if (orderSession == null || (conversation.isGroupConversation() && orderSession.getConversation().getId() != conversation.getId())) {
            conversationHelper.reply("There is no ongoing order.");
            return;
        }

        if (parser == null) {
            return;
        }

        DieciMenuItem menuItem = parser.parse(itemName);
        if (menuItem == null) {
            sendItemNotFoundMessageToUser(message, itemName);
            return;
        }

        boolean hadOrderItem = orderSession.hasOrderItem(message.getUserId());

        orderSession.updateOrderItem(message.getUserId(), new OrderItem(message.getDisplayName(), itemName, menuItem));

        String text = hadOrderItem ? "Updated order to \"" + menuItem.getArticleName() + "\" for " + message.getDisplayName() : "Added \"" + menuItem.getArticleName() + "\" to the order for " + message.getDisplayName();
        sendConfirmationMessage(conversation, message, text);
    }

    private void startOrder(Conversation conversation, ConversationHelper conversationHelper) throws BeekeeperException {
        if (orderSession != null) {
            conversationHelper.reply("There is already an ongoing order.");
            return;
        }

        orderSession = new OrderSession(conversation);
        conversationHelper.reply("Order started. Add items to the order by sending a message starting with /order, e.g., /order Quattro formaggi");
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

    protected void sendConfirmationMessage(Conversation conversation, ConversationMessage message, String text) throws BeekeeperException {
        getSdk().getConversations().sendEventMessage(conversation.getId(), text).execute();
    }

    private void sendItemNotFoundMessageToUser(ConversationMessage message, String itemName) throws BeekeeperException {
        getSdk().getConversations().sendMessageToUser(message.getUsername(), "No matching pizza found for: " + itemName).execute();
    }
}
