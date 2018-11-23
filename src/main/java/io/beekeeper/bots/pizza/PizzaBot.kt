package io.beekeeper.bots.pizza

import io.beekeeper.bots.pizza.crawler.DieciMenuItem
import io.beekeeper.bots.pizza.extensions.logger
import io.beekeeper.bots.pizza.extensions.mapIf
import io.beekeeper.bots.pizza.utils.MoneyUtil
import io.beekeeper.sdk.ChatBot
import io.beekeeper.sdk.exception.BeekeeperException
import io.beekeeper.sdk.model.ConversationMessage
import java.util.regex.Pattern


open class PizzaBot(tenantUrl: String, apiToken: String) : ChatBot(tenantUrl, apiToken) {

    private var orderSession: OrderSession? = null

    lateinit var parser: Parser<DieciMenuItem>

    private val groupConversationManager = GroupConversationManager(sdk)
    private val messenger = Messenger(sdk)

    override fun onNewMessage(message: ConversationMessage, conversationHelper: ChatBot.ConversationHelper) {
        val conversationId = message.conversationId
        try {
            if (message.text == null) {
                return
            }
            if (groupConversationManager.isGroupConversation(conversationId)) {
                processGroupMessage(conversationId, message)
            }

        } catch (e: BeekeeperException) {
            log.error("Failed to process message", e)
            try {
                messenger.sendMessage(conversationId, "Something went wrong... sorry")
            } catch (e1: BeekeeperException) {
                log.error("Failed to apologize", e1)
            }
        }
    }

    private fun processGroupMessage(conversationId: Int, message: ConversationMessage) {
        if (message.text == "/help") {
            showHelp(conversationId)
        }

        if (message.text == "/start") {
            startOrder(conversationId)
            return
        }

        val matcher = ITEM_ORDER_PATTERN.matcher(message.text)
        if (matcher.matches()) {
            if (checkValidSession(conversationId)) {
                processItemAdding(conversationId, message, matcher.group(1))
            }
            return
        }

        if (message.text == "/remove") {
            if (checkValidSession(conversationId)) {
                processRemovingItem(conversationId, message)
            }
            return
        }

        if (message.text == "/cancel") {
            if (checkValidSession(conversationId)) {
                cancelOrder(conversationId)
            }
            return
        }

        if (message.text == "/submit") {
            if (checkValidSession(conversationId)) {
                submitOrder(conversationId, message.userId)
            }
            return
        }

        if (message.text == "/confirm") {
            if (checkValidSession(conversationId)) {
                confirmOrder(conversationId, message.userId, false)
            }
            return
        }

        if (message.text == "/dryrun") {
            if (checkValidSession(conversationId)) {
                confirmOrder(conversationId, message.userId, true)
            }
            return
        }

        if (message.text == "/orders") {
            if (checkValidSession(conversationId)) {
                showOrders(conversationId)
            }
            return
        }
    }

    private fun checkValidSession(conversationId: Int): Boolean {
        if (orderSession == null || orderSession!!.conversationId != conversationId) {
            messenger.sendMessage(conversationId, "There is no ongoing order.")
            return false
        }
        return true
    }

    private fun submitOrder(conversationId: Int, userId: String) {
        val orderItems = orderSession!!.getOrderItems()
        if (orderItems.isEmpty()) {
            messenger.sendMessage(conversationId, "Nothing was added to this order yet.")
            return
        }

        val builder = "You are about to order the following:" +
                "\n" +
                getSummary(orderItems) +
                "\n\n" +
                "Type /confirm to place an order, or /cancel to keep editing your orders."

        orderSession!!.isConfirmationOngoing = true
        orderSession!!.confirmingUser = userId
        messenger.sendMessage(conversationId, builder)
    }

    private fun confirmOrder(conversationId: Int, userId: String, dryRun: Boolean) {
        if (!orderSession!!.isConfirmationOngoing) {
            messenger.sendMessage(conversationId, "You first have to /submit your order before you can confirm it.")
            return
        }

        if (userId != orderSession!!.confirmingUser) {
            messenger.sendMessage(conversationId, "Only the user who submitted the order is allowed to confirm it.")
            return
        }

        val orderItems = orderSession!!.getOrderItems()

        OrderHelper.executeOrder(orderSession!!.getOrderItems(), dryRun)
                .done {
                    try {
                        messenger.sendMessage(conversationId, "It's all good man")
                    } catch (e: BeekeeperException) {
                        log.error("Failed to send order success message", e)
                    }
                }
                .fail {
                    try {
                        messenger.sendMessage(conversationId, "Something went wrong")
                    } catch (e: BeekeeperException) {
                        log.error("Failed to send order failure message", e)
                    }
                }

        if (!dryRun) {
            orderSession = null
        }

        val builder = "Order submitted. Your food will arrive in approximately 40 minutes." +
                "\n\n" +
                "Order Summary:" +
                getSummary(orderItems)
        messenger.sendMessage(conversationId, builder)
    }

    private fun processRemovingItem(conversationId: Int, message: ConversationMessage) {
        orderSession!!.removeOrderItems(message.userId)
        val text = "Removed order for " + message.displayName + "."
        messenger.sendConfirmationMessage(conversationId, text)
    }

    private fun showHelp(conversationId: Int) {
        val helpText = "/help : show this help\n" +
                "/start : start a new pizza order\n" +
                "/cancel : cancel the current pizza order\n" +
                "/orders : show the currently registered orders\n" +
                "/order [pizza] : add a pizza with given name to the order\n" +
                "/remove : remove your order\n" +
                "/submit : submit the order to Dieci"
        messenger.sendMessage(conversationId, helpText)
    }

    private fun showOrders(conversationId: Int) {
        val orderItems = orderSession!!.getOrderItems()
        if (orderItems.isEmpty()) {
            messenger.sendMessage(conversationId, "Nothing was added to this order yet.")
            return
        }
        messenger.sendMessage(conversationId, "Current orders:\n" + getSummary(orderItems))
    }

    private fun getSummary(orderItems: Collection<OrderItem>): String {
        val builder = StringBuilder()
        var total = 0f
        for ((ordererDisplayName, menuItem) in orderItems) {
            builder
                    .append("\n- ")
                    .append(ordererDisplayName)
                    .append(": ")
                    .append(menuItem.articleName)
                    .append(" (")
                    .append(MoneyUtil.formatPrice(menuItem.price))
                    .append(")")

            total += menuItem.price
        }
        builder.append("\n")
                .append("\n")
                .append("Total: ")
                .append(MoneyUtil.formatPrice(total))

        return builder.toString()
    }

    private fun processItemAdding(conversationId: Int, message: ConversationMessage, originalText: String) {
        val rawItems = originalText.split(";| and |&|,".toRegex())

        val hadOrderItem = orderSession!!.hasOrderItem(message.userId)
        orderSession!!.removeOrderItems(message.userId)

        val orderedItems = mutableListOf<String>()

        for (rawItem in rawItems) {
            val menuItem = parser.parse(rawItem)
            if (menuItem == null) {
                sendItemNotFoundMessageToUser(message, rawItem)
                continue
            }

            orderSession!!.addOrderItem(message.userId, OrderItem(message.displayName, menuItem))

            orderedItems.add(menuItem.articleName)
        }
        if (orderedItems.isEmpty()) {
            return
        }

        val orderSummary = orderedItems.joinToString(", ")

        val text = if (hadOrderItem) {
            "Updated order to \"$orderSummary\" for ${message.displayName}"
        } else {
            "Added \"$orderSummary\" to the order for ${message.displayName}"
        }.mapIf(orderSummary.contains("hawaii", ignoreCase = true)) {
            it.plus(", who is a weirdo who likes pineapples on their pizza")
        }

        messenger.sendConfirmationMessage(conversationId, text)
    }

    private fun startOrder(conversationId: Int) {
        if (orderSession != null) {
            messenger.sendMessage(conversationId, "There is already an ongoing order in another chat. Concurrent orders are not yet supported.")
            return
        }

        orderSession = OrderSession(conversationId)
        messenger.sendMessage(conversationId, "Order started. Add items to the order by sending a message starting with /order, e.g., /order Quattro formaggi")
    }

    private fun cancelOrder(conversationId: Int) {
        if (orderSession!!.isConfirmationOngoing) {
            orderSession!!.isConfirmationOngoing = false
            orderSession!!.confirmingUser = null
            messenger.sendMessage(conversationId, "Order submission cancelled. You can now keep changing your order. Once you're happy, simply /submit it again. If you want to stop the order entirely, say /cancel again.")
        } else {
            orderSession = null
            messenger.sendMessage(conversationId, "Order cancelled. You can always /start a new one.")
        }
    }

    private fun sendItemNotFoundMessageToUser(message: ConversationMessage, itemName: String) {
        messenger.sendMessageToUser(message.username, "No matching pizza found for: $itemName")
    }

    companion object {

        private val log = logger()

        private val ITEM_ORDER_PATTERN = Pattern.compile("^/order\\s(.*)")

    }

}
