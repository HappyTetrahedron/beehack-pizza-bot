package io.beekeeper.bots.pizza.providers.dieci

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.beekeeper.bots.pizza.crawler.DieciMenuItem
import io.beekeeper.bots.pizza.dto.OrderItem
import io.beekeeper.bots.pizza.extensions.logger
import io.beekeeper.bots.pizza.extensions.mapIf
import io.beekeeper.bots.pizza.extensions.runAsync
import io.beekeeper.bots.pizza.ordering.ContactDetails
import io.beekeeper.bots.pizza.ordering.OrderHelper
import io.beekeeper.bots.pizza.shell.ProcessExecutor
import org.jdeferred2.Promise
import org.jdeferred2.impl.DeferredObject

class DieciOrderHelper : OrderHelper {

    override fun executeOrder(orderItems: Collection<OrderItem>, contactDetails: ContactDetails, dryRun: Boolean): Promise<Unit, Unit, Unit> {
        val deferred = DeferredObject<Unit, Unit, Unit>()
        val command = generateCommand(orderItems, contactDetails, dryRun)
        runAsync {
            try {
                log.info("command = $command")
                ProcessExecutor.executeCommand(command)
                        .let { result ->
                            if (result.exitCode == 0) {
                                deferred.resolve(Unit)
                            } else {
                                deferred.reject(Unit)
                            }
                        }
            } catch (e: Exception) {
                log.error("Failed to submit order", e)
                if (deferred.isPending) {
                    deferred.reject(Unit)
                }
            }
        }

        return deferred.promise()
    }

    companion object {

        private val log = logger()

        private fun generateCommand(orderItems: Collection<OrderItem>, contactDetails: ContactDetails, dryRun: Boolean) =
                listOf(
                        "node",
                        "pizza-ordering/app.js",
                        toJSON(orderItems).toString(),
                        toJSON(contactDetails).toString()
                ).mapIf(dryRun) {
                    it.plus("-dry")
                }

        private fun toJSON(orderItems: Collection<OrderItem>) =
                JsonArray().apply {
                    orderItems.forEach { item -> add(toJSON(item)) }
                }

        private fun toJSON(item: OrderItem) = JsonObject().apply {
            val menuItem = item.menuItem as? DieciMenuItem ?: throw IllegalArgumentException("Not a Dieci item")

            addProperty("articleId", menuItem.articleId)
            addProperty("articleNumber", menuItem.parentArticleNumber ?: menuItem.articleNumber)
            addProperty("commodityGroupId", menuItem.commodityGroupId)
        }

        private fun toJSON(contactDetails: ContactDetails) = JsonObject().apply {
            addProperty("firstName", contactDetails.firstName)
            addProperty("lastName", contactDetails.lastName)
            addProperty("email", contactDetails.emailAddress)
            addProperty("phone", contactDetails.phoneNumber)
        }

    }

}
