package io.beekeeper.bots.pizza.crawler;

import com.google.gson.annotations.SerializedName;

public class DieciMenuItem {

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

    public String getArticleName() {
        return articleName;
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
}
