package io.beekeeper.bots.pizza.ordering

import io.beekeeper.bots.pizza.OrderItem
import org.jdeferred2.Promise

interface OrderHelper {

    fun executeOrder(orderItems: Collection<OrderItem>, contactDetails: ContactDetails, dryRun: Boolean): Promise<Unit, Unit, Unit>

}