package io.beekeeper.bots.pizza.crawler;

import com.google.gson.annotations.SerializedName;

public class DieciMenuItem {

    public DieciMenuItem(String name) {
        this.articleName = name;
    }

    @SerializedName("article_name")
    private String articleName;
    @SerializedName("article_id")
    private String articleId;
    @SerializedName("article_desc")
    private String articleDescription;
    @SerializedName("article_articlenumber")
    private String articleNumber;
    @SerializedName("articlepicture_path")
    private String articlePictureUrl;
    @SerializedName("article_articlegroup_abbreviation")
    private String articleGroupAbbreviation;
    @SerializedName("articlepricing_price")
    private String articlePrice;

    public float getPrice() {
        try {
            return Float.parseFloat(articlePrice);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public String getArticlePrice() {
        return articlePrice;
    }

    public String getArticleName() {
        return articleName;
    }

    public String getArticleGroupAbbreviation() {
        return articleGroupAbbreviation;
    }

    public String getArticleId() {
        return articleId;
    }

    public String getArticleDescription() {
        return articleDescription;
    }

    public String getArticleNumber() {
        return articleNumber;
    }

    public String getArticlePictureUrl() {
        return articlePictureUrl;
    }

    public String getKey() {
        return articleName + " " + articleGroupAbbreviation;
    }
}