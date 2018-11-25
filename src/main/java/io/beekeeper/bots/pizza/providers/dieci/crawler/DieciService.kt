package io.beekeeper.bots.pizza.providers.dieci.crawler

import com.google.gson.Gson
import com.google.gson.JsonParser
import io.beekeeper.bots.pizza.extensions.logger
import io.beekeeper.bots.pizza.utils.HttpUtil
import java.io.IOException

class DieciService {

    private var headers: MutableMap<String, String> = mutableMapOf()

    @Throws(IOException::class)
    fun initializeSession() {
        val response = HttpUtil.doGet(INIT_URL)
        val cookies = response.headers["Set-Cookie"] ?: emptyList()

        headers.clear()
        for (cookie in cookies) {
            headers["Cookie"] = cookie.split(";".toRegex(), 2).toTypedArray()[0]
        }
        headers["Referer"] = FAKE_REFER_URL

        HttpUtil.doGet(CHANGE_LANGUAGE_URL, headers)
    }

    @Throws(IOException::class)
    fun fetchAllDieciPages(): List<DieciMenuItem> =
            PAGE_PATHS.flatMap { pagePath ->
                fetchDieciPage(pagePath)
            }

    @Throws(IOException::class)
    private fun fetchDieciPage(page: String): List<DieciMenuItem> {
        log.info("Crawling Dieci page: $MENU_BASE_URL$page")
        val response = HttpUtil.doGet(MENU_BASE_URL + page, this.headers)
        return parsePage(response.response)
    }

    private fun parsePage(htmlPage: String): List<DieciMenuItem> =
            htmlPage
                    .split("\n".toRegex())
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .firstOrNull { line ->
                        line.startsWith("var articles = {")
                    }
                    ?.let { line ->
                        parseArticles(line)
                    }
                    ?: emptyList()

    private fun parseArticles(line: String): List<DieciMenuItem> {
        val gson = Gson()
        val json = line.substring(line.indexOf('{'), line.length - 1)

        return JsonParser()
                .parse(json)
                .asJsonObject
                .entrySet()
                .flatMap { (_, value) ->
                    val rootItem = value.asJsonObject
                    val articleGroups = rootItem.getAsJsonObject("articlegroup")
                    if (articleGroups == null) {
                        val menuItem = gson.fromJson(rootItem.toString(), DieciMenuItem::class.java)
                        listOf(menuItem)
                    } else {
                        val parentArticleNumber = rootItem.getAsJsonPrimitive("article_articlenumber").asString
                        val parentCommodityGroupId = rootItem.getAsJsonPrimitive("commoditygroup_id").asString

                        articleGroups
                                .entrySet()
                                .map { (_, value1) ->
                                    gson
                                            .fromJson(value1.toString(), DieciMenuItem::class.java)
                                            .apply {
                                                this.parentArticleNumber = parentArticleNumber
                                                this.commodityGroupId = parentCommodityGroupId
                                            }
                                }
                    }
                }
    }

    companion object {

        private val log = logger()

        private const val MENU_BASE_URL = "https://webshop.dieci.ch/c/"
        private const val FAKE_REFER_URL = "https://webshop.dieci.ch/c/1/pizza"
        private const val CHANGE_LANGUAGE_URL = "https://webshop.dieci.ch/changeLanguage?lang=en_US"
        private const val INIT_URL = "https://webshop.dieci.ch/store/Z%C3%BCrich%20links%20der%20Limmat"

        private val PAGE_PATHS = arrayOf(
                "1/pizza",
                "2/pasta",
                "3/salate",
                "6/getraenke",
                "7/gelati-desserts",
                "58/glutenfreie-pizza"
        )
    }

}
