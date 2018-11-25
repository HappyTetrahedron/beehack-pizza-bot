package io.beekeeper.bots.pizza.providers.dieci

import io.beekeeper.bots.pizza.ordering.OrderHelper
import io.beekeeper.bots.pizza.ordering.OrderHelperFactory

class DieciOrderHelperFactory : OrderHelperFactory {

    override fun newOrderHelper(): OrderHelper = DieciOrderHelper()

}