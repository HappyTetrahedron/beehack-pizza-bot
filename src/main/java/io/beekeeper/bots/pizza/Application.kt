package io.beekeeper.bots.pizza

import io.beekeeper.bots.pizza.beekeeper.BeekeeperChatListener
import io.beekeeper.bots.pizza.beekeeper.BeekeeperContactDetailsProvider
import io.beekeeper.bots.pizza.beekeeper.BeekeeperMessenger
import io.beekeeper.bots.pizza.beekeeper.GroupConversationManager
import io.beekeeper.bots.pizza.crawler.DieciMenuItem
import io.beekeeper.bots.pizza.crawler.DieciService
import io.beekeeper.bots.pizza.extensions.logger
import io.beekeeper.bots.pizza.providers.dieci.DieciOrderHelperFactory
import io.beekeeper.sdk.BeekeeperSDK
import java.io.IOException

object Application {

    private val log = logger()

    // TODO: Move these out into env variables
    private val BASE_URL = "https://team.beekeeper.io"
    private val API_TOKEN = "TODO"

    @JvmStatic
    fun main(args: Array<String>) {
        setupPizzaBot(BASE_URL, API_TOKEN)
    }

    private fun setupPizzaBot(baseUrl: String, apiToken: String) {
        // Beekeeper stuff
        val sdk = BeekeeperSDK.newInstance(baseUrl, apiToken)
        val chatListener = BeekeeperChatListener(sdk)
        val messenger = BeekeeperMessenger(sdk)
        val groupConversationManager = GroupConversationManager(sdk)
        val contactDetailsProvider = BeekeeperContactDetailsProvider(sdk)

        // Dieci stuff
        val parser = initDieciMenuParser()
        val orderHelperFactory = DieciOrderHelperFactory()

        val bot = PizzaBot(
                messenger = messenger,
                contactDetailsProvider = contactDetailsProvider,
                articleParser = parser,
                orderHelperFactory = orderHelperFactory
        )

        log.info("Registering message listener")
        chatListener.register { message ->
            if (groupConversationManager.isGroupConversation(message.chat.conversationId) == true) {
                bot.onNewMessage(message)
            }
        }
        chatListener.start()
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
