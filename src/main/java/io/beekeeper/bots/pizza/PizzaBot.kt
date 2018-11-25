package io.beekeeper.bots.pizza

import io.beekeeper.bots.pizza.dto.*
import io.beekeeper.bots.pizza.extensions.logger
import io.beekeeper.bots.pizza.extensions.mapIf
import io.beekeeper.bots.pizza.messenger.Messenger
import io.beekeeper.bots.pizza.messenger.MessengerException
import io.beekeeper.bots.pizza.ordering.ContactDetailsException
import io.beekeeper.bots.pizza.ordering.ContactDetailsProvider
import io.beekeeper.bots.pizza.ordering.OrderHelperFactory
import io.beekeeper.bots.pizza.utils.MoneyUtil
import java.util.regex.Pattern


open class PizzaBot(
        private val messenger: Messenger,
        private val contactDetailsProvider: ContactDetailsProvider,
        private val articleParser: Parser<out MenuItem>,
        private val orderHelperFactory: OrderHelperFactory
) {

    private val sessionManager = OrderSessionManager()

    fun onNewMessage(message: Message) {
        try {
            processMessage(message.chat, message)
        } catch (e: MessengerException) {
            log.error("Failed to process message", e)
            try {
                sendMessage(message.chat, "Something went wrong... sorry")
            } catch (e1: MessengerException) {
                log.error("Failed to apologize", e1)
            }
        }
    }

    private fun processMessage(chat: Chat, message: Message) {
        if (message.text == "/help") {
            showHelp(chat)
        }

        if (message.text == "/start") {
            startOrder(chat)
            return
        }

        val matcher = ITEM_ORDER_PATTERN.matcher(message.text)
        if (matcher.matches()) {
            getSessionOrFail(chat)?.let { session ->
                processItemAdding(session, message, matcher.group(1))
            }
            return
        }

        if (message.text == "/remove") {
            getSessionOrFail(chat)?.let { session ->
                processRemovingItem(session, message)
            }
            return
        }

        if (message.text == "/cancel") {
            getSessionOrFail(chat)?.let { session ->
                cancelOrder(session)
            }
            return
        }

        if (message.text == "/submit") {
            getSessionOrFail(chat)?.let { session ->
                submitOrder(session, message.sender)
            }
            return
        }

        if (message.text == "/confirm") {
            getSessionOrFail(chat)?.let { session ->
                confirmOrder(session, message.sender, dryRun = false)
            }
            return
        }

        if (message.text == "/dryrun") {
            getSessionOrFail(chat)?.let { session ->
                confirmOrder(session, message.sender, dryRun = true)
            }
            return
        }

        if (message.text == "/orders") {
            getSessionOrFail(chat)?.let { session ->
                showOrders(session)
            }
            return
        }
    }

    private fun getSessionOrFail(chat: Chat): OrderSession? {
        return sessionManager.getSession(chat) ?: run {
            sendMessage(chat, "There is no ongoing order. You have to /start a new one first.")
            null
        }
    }

    private fun submitOrder(session: OrderSession, sender: User) {
        val chat = session.chat
        val orderItems = session.getOrderItems()
        if (orderItems.isEmpty()) {
            sendMessage(chat, "Nothing was added to this order yet.")
            return
        }

        val builder = "You are about to order the following:" +
                "\n" +
                getSummary(orderItems) +
                "\n\n" +
                "Type /confirm to place an order, or /cancel to keep editing your orders."

        session.isConfirmationOngoing = true
        session.confirmingUser = sender
        sendMessage(chat, builder)
    }

    private fun confirmOrder(session: OrderSession, sender: User, dryRun: Boolean) {
        val chat = session.chat
        if (!session.isConfirmationOngoing) {
            sendMessage(chat, "You first have to /submit your order before you can confirm it.")
            return
        }

        if (sender.id != session.confirmingUser?.id) {
            sendMessage(chat, "Only the user who submitted the order is allowed to confirm it.")
            return
        }

        val orderItems = session.getOrderItems()
        val contactDetails = try {
            contactDetailsProvider.getContactDetails(sender.username)
        } catch (e: ContactDetailsException) {
            sendMessage(chat, "Failed so submit order: ${e.message}")
            return
        }

        log.debug("Starting to submit the order form")
        // TODO: Change state of orderSession to prevent multiple submissions in parallel
        orderHelperFactory.newOrderHelper()
                .executeOrder(session.getOrderItems(), contactDetails, dryRun)
                .done {
                    log.debug("Order submission completed")
                    try {
                        // TODO: Retrieve the wait time from the OrderHelper
                        sendMessage(chat, "It's all good man. Your food will arrive in approximately 40 minutes.")
                    } catch (e: MessengerException) {
                        log.error("Failed to send order success message", e)
                    }
                }
                .fail {
                    log.warn("Order submission failed")
                    try {
                        sendMessage(chat, "Something went wrong")
                    } catch (e: MessengerException) {
                        log.error("Failed to send order failure message", e)
                    }
                }

        if (!dryRun) {
            // TODO: Only clear session after success, but freeze it during submission
            sessionManager.deleteSession(session)
        }

        val builder = "Ordering now... Please wait." +
                "\n\n" +
                "Order Summary:" +
                getSummary(orderItems)
        sendMessage(chat, builder)
    }

    private fun processRemovingItem(session: OrderSession, message: Message) {
        val chat = session.chat
        session.removeOrderItems(message.sender)
        val text = "Removed order for ${message.sender.displayName}."
        sendEventMessage(chat, text)
    }

    private fun showHelp(chat: Chat) {
        val helpText = "/help : show this help\n" +
                "/start : start a new pizza order\n" +
                "/cancel : cancel the current pizza order\n" +
                "/orders : show the currently registered orders\n" +
                "/order [pizza] : add a pizza with given name to the order\n" +
                "/remove : remove your order\n" +
                "/submit : submit the order (requires confirmation)"
        sendMessage(chat, helpText)
    }

    private fun showOrders(session: OrderSession) {
        val chat = session.chat
        val orderItems = session.getOrderItems()
        if (orderItems.isEmpty()) {
            sendMessage(chat, "Nothing was added to this order yet.")
            return
        }
        sendMessage(chat, "Current orders:\n" + getSummary(orderItems))
    }

    private fun getSummary(orderItems: Collection<OrderItem>): String {
        val builder = StringBuilder()
        var total = 0f

        for (orderItem in orderItems) {
            val price = orderItem.itemPrice
            builder
                    .append("\n- ")
                    .append(orderItem.user.displayName)
                    .append(": ")
                    .append(orderItem.itemName)
                    .mapIf(price != null) {
                        it.append(" (")
                                .append(MoneyUtil.formatPrice(price!!))
                                .append(")")
                    }

            if (price != null) {
                total += price
            }
        }
        builder.append("\n")
                .append("\n")
                .append("Total: ")
                .append(MoneyUtil.formatPrice(total))

        return builder.toString()
    }

    private fun processItemAdding(session: OrderSession, message: Message, originalText: String) {
        val chat = session.chat
        val rawItems = originalText.split(";| and |&|,".toRegex())

        val hadOrderItem = session.hasOrderItem(message.sender)
        session.removeOrderItems(message.sender)

        val orderedItems = mutableListOf<String>()

        for (rawItem in rawItems) {
            val menuItem = articleParser.parse(rawItem)
            if (menuItem == null) {
                sendItemNotFoundMessageToUser(message, rawItem)
                continue
            }

            session.addOrderItem(message.sender, OrderItem(message.sender, menuItem))

            orderedItems.add(menuItem.articleName)
        }
        if (orderedItems.isEmpty()) {
            return
        }

        val orderSummary = orderedItems.joinToString(", ")

        val text = if (hadOrderItem) {
            "Updated order to \"$orderSummary\" for ${message.sender.displayName}"
        } else {
            "Added \"$orderSummary\" to the order for ${message.sender.displayName}"
        }.mapIf(orderSummary.contains("hawaii", ignoreCase = true)) {
            it.plus(", who is a weirdo who likes pineapples on their pizza")
        }

        sendEventMessage(chat, text)
    }

    private fun startOrder(chat: Chat) {
        if (sessionManager.getSession(chat) != null) {
            sendMessage(chat, "There is already an ongoing order.")
            return
        }

        sessionManager.createSession(chat)
        sendMessage(chat, "Order started. Add items to the order by sending a message starting with /order, e.g., /order Quattro formaggi")
    }

    private fun cancelOrder(session: OrderSession) {
        val chat = session.chat
        if (session.isConfirmationOngoing) {
            session.isConfirmationOngoing = false
            session.confirmingUser = null
            sendMessage(chat, "Order submission cancelled. You can now keep changing your order. Once you're happy, simply /submit it again. If you want to stop the order entirely, say /cancel again.")
        } else {
            sessionManager.deleteSession(session)
            sendMessage(chat, "Order cancelled. You can always /start a new one.")
        }
    }

    private fun sendItemNotFoundMessageToUser(message: Message, itemName: String) {
        messenger.sendMessageToUser(message.sender, "No matching pizza found for: $itemName")
    }

    private fun sendMessage(chat: Chat, text: String) {
        messenger.sendMessage(chat, text)
    }

    private fun sendEventMessage(chat: Chat, text: String) {
        messenger.sendEventMessage(chat, text)
    }

    companion object {

        private val log = logger()

        private val ITEM_ORDER_PATTERN = Pattern.compile("^/order\\s(.*)")

    }

}
