package io.beekeeper.bots.pizza

import io.beekeeper.bots.pizza.crawler.DieciMenuItem
import io.beekeeper.bots.pizza.crawler.DieciService
import io.beekeeper.bots.pizza.extensions.logger
import java.io.IOException

object Application {

    private val log = logger()

    // TODO: Move these out into env variables
    private val BASE_URL = "https://team.beekeeper.io"
    private val API_TOKEN = "975f909c-7344-4123-89d8-d81b63dc641b"

    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val bot = PizzaBot(BASE_URL, API_TOKEN)

        bot.parser = initDieciMenuParser()

        log.info("Starting bot")
        bot.start()
    }

    @Throws(IOException::class)
    private fun initDieciMenuParser(): Parser<DieciMenuItem> {
        val dieciService = DieciService()
        dieciService.initializeSession()
        val result = dieciService.fetchAllDieciPages()
        log.info("${result.size} menu items found")

        val mapItems = result
                .asSequence()
                .associate { it.key to it }

        return Parser(mapItems)
    }

}
