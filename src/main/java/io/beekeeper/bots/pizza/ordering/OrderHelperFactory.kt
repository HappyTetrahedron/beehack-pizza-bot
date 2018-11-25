package io.beekeeper.bots.pizza.ordering

interface OrderHelperFactory {

    fun newOrderHelper(): OrderHelper

}