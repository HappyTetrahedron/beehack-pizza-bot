package io.beekeeper.bots.pizza.crawler

import com.google.gson.annotations.SerializedName

import io.beekeeper.bots.pizza.dto.MenuItem

class DieciMenuItem : MenuItem {

    @SerializedName("article_name")
    override val articleName: String = ""
    @SerializedName("article_id")
    val articleId: String? = null
    @SerializedName("article_desc")
    val articleDescription: String? = null
    @SerializedName("article_articlenumber")
    val articleNumber: String? = null
    @SerializedName("articlepicture_path")
    val articlePictureUrl: String? = null
    @SerializedName("article_articlegroup_abbreviation")
    val articleGroupAbbreviation: String? = null
    @SerializedName("articlepricing_price")
    val articlePrice: String? = null
    @SerializedName("commoditygroup_id")
    var commodityGroupId: String? = null

    var parentArticleNumber: String? = null

    override val price: Float?
        get() {
            try {
                return java.lang.Float.parseFloat(articlePrice!!)
            } catch (e: NumberFormatException) {
                return null
            }
        }

    val key: String
        get() = "$articleName $articleGroupAbbreviation"
}
