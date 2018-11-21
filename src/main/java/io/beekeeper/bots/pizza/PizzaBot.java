package io.beekeeper.bots.pizza;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.beekeeper.bots.pizza.crawler.DieciMenuItem;
import io.beekeeper.sdk.ChatBot;
import io.beekeeper.sdk.exception.BeekeeperException;
import io.beekeeper.sdk.model.ConversationMessage;


public class PizzaBot extends ChatBot {

    private final Pattern ITEM_ORDER_PATTERN = Pattern.compile("^/order\\s(.*)");

    private OrderSession orderSession = null;

    private Parser<DieciMenuItem> parser = null;

    public PizzaBot(String tenantUrl, String apiToken) {
        super(tenantUrl, apiToken);
    }

    private final GroupConversationManager groupConversationManager = new GroupConversationManager(this.getSdk());

    public void setParser(Parser<DieciMenuItem> parser) {
        this.parser = parser;
    }

    @Override
    public void onNewMessage(ConversationMessage message, ConversationHelper conversationHelper) {
        try {
            if (message.getText() == null) {
                return;
            }

            int conversationId = message.getConversationId();
            if (groupConversationManager.isGroupConversation(conversationId)) {
                processGroupMessage(conversationId, message, conversationHelper);
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

    void processGroupMessage(int conversationId, ConversationMessage message, ConversationHelper conversationHelper) throws BeekeeperException {
        if (message.getText().equals("/help")) {
            showHelp(conversationHelper);
        }

        if (message.getText().equals("/start")) {
            startOrder(conversationId, conversationHelper);
            return;
        }

        Matcher matcher = ITEM_ORDER_PATTERN.matcher(message.getText());
        if (matcher.matches()) {
            if (checkValidSession(conversationId, conversationHelper)) {
                processItemAdding(conversationId, message, matcher.group(1), conversationHelper);
            }
            return;
        }

        if (message.getText().equals("/remove")) {
            if (checkValidSession(conversationId, conversationHelper)) {
                processRemovingItem(conversationId, message, conversationHelper);
            }
            return;
        }

        if (message.getText().equals("/cancel")) {
            if (checkValidSession(conversationId, conversationHelper)) {
                cancelOrder(conversationId, conversationHelper);
            }
            return;
        }

        System.out.println(message.getUserId());

        if (message.getText().equals("/submit")) {
            if (checkValidSession(conversationId, conversationHelper)) {
                submitOrder(message.getUserId(), conversationHelper);
            }
            return;
        }

        if (message.getText().equals("/confirm")) {
            if (checkValidSession(conversationId, conversationHelper)) {
                confirmOrder(message.getUserId(), conversationHelper, false);
            }
            return;
        }

        if (message.getText().equals("/dryrun")) {
            if (checkValidSession(conversationId, conversationHelper)) {
                confirmOrder(message.getUserId(), conversationHelper, true);
            }
            return;
        }

        if (message.getText().equals("/orders")) {
            if (checkValidSession(conversationId, conversationHelper)) {
                showOrders(conversationId, conversationHelper);
            }
            return;
        }
    }

    private boolean checkValidSession(int conversationId, ConversationHelper conversationHelper) throws BeekeeperException {
        if (orderSession == null || orderSession.getConversationId() != conversationId) {
            conversationHelper.reply("There is no ongoing order.");
            return false;
        }
        return true;
    }


    private void submitOrder(String userId, ConversationHelper conversationHelper) throws BeekeeperException {
        Collection<OrderItem> orderItems = orderSession.getOrderItems();
        if (orderItems.isEmpty()) {
            conversationHelper.reply("Nothing was added to this order yet.");
            return;
        }

        String builder = "You are about to order the following:" +
                "\n" +
                getSummary(orderItems) +
                "\n\n" +
                "Type /confirm to place an order, or /cancel to keep editing your orders.";

        orderSession.setConfirmationOngoing(true);
        orderSession.setConfirmingUser(userId);
        conversationHelper.reply(builder);
    }

    private void confirmOrder(String userId, ConversationHelper conversationHelper, boolean dryrun) throws BeekeeperException {
        if (!orderSession.isConfirmationOngoing()) {
            conversationHelper.reply("You first have to /submit your order so you can confirm it.");
            return;
        }

        if (userId.equals(orderSession.getConfirmingUser())) {

            Collection<OrderItem> orderItems = orderSession.getOrderItems();

            OrderHelper.executeOrder(orderSession.getOrderItems(), dryrun, new OrderHelper.Callback() {
                @Override
                public void onSuccess() {
                    try {
                        conversationHelper.reply("It's all good man");
                    } catch (BeekeeperException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure() {
                    try {
                        conversationHelper.reply("Something went wrong");
                    } catch (BeekeeperException e) {
                        e.printStackTrace();
                    }
                }
            });

            if (!dryrun) {
                orderSession = null;
            }

            String builder = "Order submitted. Your food will arrive in approximately 40 minutes." +
                    "\n\n" +
                    "Order Summary:" +
                    getSummary(orderItems);
            conversationHelper.reply(builder);
        }
        else {
            conversationHelper.reply("Only the user who submitted the order is allowed to confirm it.");
        }
    }

    private void processRemovingItem(int conversationId, ConversationMessage message, ConversationHelper conversationHelper) throws BeekeeperException {
        orderSession.removeOrderItems(message.getUserId());
        String text = "Removed order for " + message.getDisplayName() + ".";
        sendConfirmationMessage(conversationId, text);
    }

    private void showHelp(ConversationHelper conversationHelper) throws BeekeeperException {
        String helpText =
                "/help : show this help\n" +
                        "/start : start a new pizza order\n" +
                        "/cancel : cancel the current pizza order\n" +
                        "/orders : show the currently registered orders\n" +
                        "/order [pizza] : add a pizza with given name to the order\n" +
                        "/remove : remove your order\n" +
                        "/submit : submit the order to Dieci";
        conversationHelper.reply(helpText);
    }

    private void showOrders(int conversationId, ConversationHelper conversationHelper) throws BeekeeperException {
        Collection<OrderItem> orderItems = orderSession.getOrderItems();
        if (orderItems.isEmpty()) {
            conversationHelper.reply("Nothing was added to this order yet.");
            return;
        }
        conversationHelper.reply("Current orders:\n" + getSummary(orderItems));
    }

    private String getSummary(Collection<OrderItem> orderItems) {
        StringBuilder builder = new StringBuilder();
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

        return builder.toString();
    }

    private String formatPrice(float price) {
        return String.format("%.2f", price);
    }

    private void processItemAdding(int conversationId, ConversationMessage message, String originalText, ConversationHelper conversationHelper) throws BeekeeperException {
        if (parser == null) {
            return;
        }

        String[] rawItems = originalText.split(";| and | & ");

        boolean hadOrderItem = orderSession.hasOrderItem(message.getUserId());
        orderSession.removeOrderItems(message.getUserId());

        StringBuilder stringBuilder = new StringBuilder();
        boolean first = true;

        for (String rawItem : rawItems) {
            DieciMenuItem menuItem = parser.parse(rawItem);
            if (menuItem == null) {
                sendItemNotFoundMessageToUser(message, rawItem);
                continue;
            }

            orderSession.addOrderItem(message.getUserId(), new OrderItem(message.getDisplayName(), menuItem));

            if (first) {
                first = false;
            } else {
                stringBuilder.append(", ");
            }
            stringBuilder.append(menuItem.getArticleName());
        }

        String orderSummary = stringBuilder.toString();

        if (orderSummary.isEmpty()) {
            return;
        }

        String text = hadOrderItem ? "Updated order to \"" + orderSummary + "\" for " + message.getDisplayName() : "Added \"" + orderSummary + "\" to the order for " + message.getDisplayName();

        if (orderSummary.toLowerCase().contains("hawaii")) text = text + ", who is a weirdo that likes pineapples on their pizza";

        sendConfirmationMessage(conversationId, text);
    }

    private void startOrder(int conversationId, ConversationHelper conversationHelper) throws BeekeeperException {
        if (orderSession != null) {
            conversationHelper.reply("There is already an ongoing order in another chat. Concurrent orders are not yet supported.");
            return;
        }

        orderSession = new OrderSession(conversationId);
        conversationHelper.reply("Order started. Add items to the order by sending a message starting with /order, e.g., /order Quattro formaggi");
    }

    private void cancelOrder(int conversationId, ConversationHelper conversationHelper) throws BeekeeperException {
        if (orderSession.isConfirmationOngoing()) {
            orderSession.setConfirmationOngoing(false);
            orderSession.setConfirmingUser("");
            conversationHelper.reply("Order submission cancelled. You can now keep changing your order. Once you're happy, simply /submit it again. If you want to stop the order entirely, say /cancel again.");
        }
        else {
            orderSession = null;
            conversationHelper.reply("Order cancelled. You can always /start a new one.");
        }
    }

    private void sendConfirmationMessage(int conversationId, String text) throws BeekeeperException {
        getSdk().getConversations().sendEventMessage(conversationId, text).execute();
    }

    private void sendItemNotFoundMessageToUser(ConversationMessage message, String itemName) throws BeekeeperException {
        getSdk().getConversations().sendMessageToUser(message.getUsername(), "No matching pizza found for: " + itemName).execute();
    }
}
