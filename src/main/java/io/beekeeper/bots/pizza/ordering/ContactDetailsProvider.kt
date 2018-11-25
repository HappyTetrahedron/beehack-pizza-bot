package io.beekeeper.bots.pizza.ordering

interface ContactDetailsProvider {

    fun getContactDetails(username: String): ContactDetails

}