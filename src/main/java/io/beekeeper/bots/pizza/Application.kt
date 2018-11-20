package io.beekeeper.bots.pizza

import io.beekeeper.bots.pizza.crawler.DieciMenuItem
import io.beekeeper.bots.pizza.crawler.DieciService
import java.io.IOException

object Application {

    // TODO: Move these out into env variables
    private val BASE_URL = "https://team.beekeeper.io"
    private val API_TOKEN = "975f909c-7344-4123-89d8-d81b63dc641b"

    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val bot = PizzaBot(BASE_URL, API_TOKEN)

        val dieciMenuItemParser = initDieciMenuParser()
        bot.setParser(dieciMenuItemParser)

        println("Starting bot")
        bot.start()
    }

    @Throws(IOException::class)
    private fun initDieciMenuParser(): Parser<DieciMenuItem> {
        val dieciService = DieciService()
        dieciService.initializeSession()
        val result = dieciService.fetchAllDieciPages()
        println("${result.size} menu items found")

        val mapItems = result
                .asSequence()
                .associate { it.key to it }

        return Parser(mapItems)
    }

}
