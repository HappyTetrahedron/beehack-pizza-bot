package io.beekeeper.bots.pizza

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.beekeeper.bots.pizza.extensions.logger
import io.beekeeper.bots.pizza.extensions.mapIf
import io.beekeeper.bots.pizza.extensions.runAsync
import io.beekeeper.bots.pizza.shell.ProcessExecutor
import org.jdeferred2.Promise
import org.jdeferred2.impl.DeferredObject

object OrderHelper {

    private val log = logger()

    fun executeOrder(orderItems: Collection<OrderItem>, dryRun: Boolean): Promise<Unit, Unit, Unit> {
        val deferred = DeferredObject<Unit, Unit, Unit>()
        val command = generateCommand(orderItems, dryRun)
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

    private fun generateCommand(orderItems: Collection<OrderItem>, dryRun: Boolean) =
            listOf(
                    "node",
                    "pizza-ordering/app.js",
                    toJSON(orderItems).toString()
            ).mapIf(!dryRun) {
                it.plus("-x")
            }

    private fun toJSON(orderItems: Collection<OrderItem>) =
            JsonArray().apply {
                orderItems.forEach { item -> add(toJSON(item)) }
            }

    private fun toJSON(item: OrderItem) = JsonObject().apply {
        addProperty("articleId", item.menuItem.articleId)
        addProperty("articleNumber", item.menuItem.parentArticleNumber ?: item.menuItem.articleNumber)
        addProperty("commodityGroupId", item.menuItem.commodityGroupId)
    }
}
