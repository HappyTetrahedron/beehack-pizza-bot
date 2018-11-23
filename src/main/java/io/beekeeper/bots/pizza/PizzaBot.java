package io.beekeeper.bots.pizza;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.beekeeper.bots.pizza.crawler.DieciMenuItem;
import io.beekeeper.bots.pizza.utils.MoneyUtil;
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

    private final GroupConversationManager groupConversationManager = new GroupConversationManager(getSdk());
    private final Messenger messenger = new Messenger(getSdk());

    public void setParser(Parser<DieciMenuItem> parser) {
        this.parser = parser;
    }

    @Override
    public void onNewMessage(ConversationMessage message, ConversationHelper conversationHelper) {
        int conversationId = message.getConversationId();
        try {
            if (message.getText() == null) {
                return;
            }
            if (groupConversationManager.isGroupConversation(conversationId)) {
                processGroupMessage(conversationId, message);
            }

        } catch (BeekeeperException e) {
            e.printStackTrace();
            try {
                messenger.sendMessage(conversationId, "Something went wrong... sorry");
            } catch (BeekeeperException e1) {
                e1.printStackTrace();
            }
        }
    }

    void processGroupMessage(int conversationId, ConversationMessage message) throws BeekeeperException {
        if (message.getText().equals("/help")) {
            showHelp(conversationId);
        }

        if (message.getText().equals("/start")) {
            startOrder(conversationId);
            return;
        }

        Matcher matcher = ITEM_ORDER_PATTERN.matcher(message.getText());
        if (matcher.matches()) {
            if (checkValidSession(conversationId)) {
                processItemAdding(conversationId, message, matcher.group(1));
            }
            return;
        }

        if (message.getText().equals("/remove")) {
            if (checkValidSession(conversationId)) {
                processRemovingItem(conversationId, message);
            }
            return;
        }

        if (message.getText().equals("/cancel")) {
            if (checkValidSession(conversationId)) {
                cancelOrder(conversationId);
            }
            return;
        }

        if (message.getText().equals("/submit")) {
            if (checkValidSession(conversationId)) {
                submitOrder(conversationId, message.getUserId());
            }
            return;
        }

        if (message.getText().equals("/confirm")) {
            if (checkValidSession(conversationId)) {
                confirmOrder(conversationId, message.getUserId(), false);
            }
            return;
        }

        if (message.getText().equals("/dryrun")) {
            if (checkValidSession(conversationId)) {
                confirmOrder(conversationId, message.getUserId(), true);
            }
            return;
        }

        if (message.getText().equals("/orders")) {
            if (checkValidSession(conversationId)) {
                showOrders(conversationId);
            }
            return;
        }
    }

    private boolean checkValidSession(int conversationId) throws BeekeeperException {
        if (orderSession == null || orderSession.getConversationId() != conversationId) {
            messenger.sendMessage(conversationId, "There is no ongoing order.");
            return false;
        }
        return true;
    }


    private void submitOrder(int conversationId, String userId) throws BeekeeperException {
        Collection<OrderItem> orderItems = orderSession.getOrderItems();
        if (orderItems.isEmpty()) {
            messenger.sendMessage(conversationId, "Nothing was added to this order yet.");
            return;
        }

        String builder = "You are about to order the following:" +
                "\n" +
                getSummary(orderItems) +
                "\n\n" +
                "Type /confirm to place an order, or /cancel to keep editing your orders.";

        orderSession.setConfirmationOngoing(true);
        orderSession.setConfirmingUser(userId);
        messenger.sendMessage(conversationId, builder);
    }

    private void confirmOrder(int conversationId, String userId, boolean dryrun) throws BeekeeperException {
        if (!orderSession.isConfirmationOngoing()) {
            messenger.sendMessage(conversationId, "You first have to /submit your order before you can confirm it.");
            return;
        }

        if (!userId.equals(orderSession.getConfirmingUser())) {
            messenger.sendMessage(conversationId, "Only the user who submitted the order is allowed to confirm it.");
            return;
        }

        Collection<OrderItem> orderItems = orderSession.getOrderItems();

        OrderHelper.executeOrder(orderSession.getOrderItems(), dryrun)
                .done(result -> {
                    try {
                        messenger.sendMessage(conversationId, "It's all good man");
                    } catch (BeekeeperException e) {
                        e.printStackTrace();
                    }
                })
                .fail(result -> {
                    try {
                        messenger.sendMessage(conversationId, "Something went wrong");
                    } catch (BeekeeperException e) {
                        e.printStackTrace();
                    }
                });

        if (!dryrun) {
            orderSession = null;
        }

        String builder = "Order submitted. Your food will arrive in approximately 40 minutes." +
                "\n\n" +
                "Order Summary:" +
                getSummary(orderItems);
        messenger.sendMessage(conversationId, builder);

    }

    private void processRemovingItem(int conversationId, ConversationMessage message) throws BeekeeperException {
        orderSession.removeOrderItems(message.getUserId());
        String text = "Removed order for " + message.getDisplayName() + ".";
        messenger.sendConfirmationMessage(conversationId, text);
    }

    private void showHelp(int conversationId) throws BeekeeperException {
        String helpText =
                "/help : show this help\n" +
                        "/start : start a new pizza order\n" +
                        "/cancel : cancel the current pizza order\n" +
                        "/orders : show the currently registered orders\n" +
                        "/order [pizza] : add a pizza with given name to the order\n" +
                        "/remove : remove your order\n" +
                        "/submit : submit the order to Dieci";
        messenger.sendMessage(conversationId, helpText);
    }

    private void showOrders(int conversationId) throws BeekeeperException {
        Collection<OrderItem> orderItems = orderSession.getOrderItems();
        if (orderItems.isEmpty()) {
            messenger.sendMessage(conversationId, "Nothing was added to this order yet.");
            return;
        }
        messenger.sendMessage(conversationId, "Current orders:\n" + getSummary(orderItems));
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
                    .append(MoneyUtil.INSTANCE.formatPrice(orderItem.getMenuItem().getPrice()))
                    .append(")");

            total += orderItem.getMenuItem().getPrice();
        }
        builder.append("\n")
                .append("\n")
                .append("Total: ")
                .append(MoneyUtil.INSTANCE.formatPrice(total));

        return builder.toString();
    }

    private void processItemAdding(int conversationId, ConversationMessage message, String originalText) throws BeekeeperException {
        if (parser == null) {
            return;
        }

        String[] rawItems = originalText.split(";| and |&|,");

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

        if (orderSummary.toLowerCase().contains("hawaii")) {
            text = text + ", who is a weirdo that likes pineapples on their pizza";
        }

        messenger.sendConfirmationMessage(conversationId, text);
    }

    private void startOrder(int conversationId) throws BeekeeperException {
        if (orderSession != null) {
            messenger.sendMessage(conversationId, "There is already an ongoing order in another chat. Concurrent orders are not yet supported.");
            return;
        }

        orderSession = new OrderSession(conversationId);
        messenger.sendMessage(conversationId, "Order started. Add items to the order by sending a message starting with /order, e.g., /order Quattro formaggi");
    }

    private void cancelOrder(int conversationId) throws BeekeeperException {
        if (orderSession.isConfirmationOngoing()) {
            orderSession.setConfirmationOngoing(false);
            orderSession.setConfirmingUser("");
            messenger.sendMessage(conversationId, "Order submission cancelled. You can now keep changing your order. Once you're happy, simply /submit it again. If you want to stop the order entirely, say /cancel again.");
        } else {
            orderSession = null;
            messenger.sendMessage(conversationId, "Order cancelled. You can always /start a new one.");
        }
    }

    private void sendItemNotFoundMessageToUser(ConversationMessage message, String itemName) throws BeekeeperException {
        messenger.sendMessageToUser(message.getUsername(), "No matching pizza found for: " + itemName);
    }
}
