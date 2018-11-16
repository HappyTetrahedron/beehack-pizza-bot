package io.beekeeper.bots.pizza.crawler;

import com.google.gson.annotations.SerializedName;

public class DieciMenuItem {

    public DieciMenuItem(String name) {
        this.articleName = name;
    }

    @SerializedName("article_name")
    private String articleName;

    public String getArticleName() {
        return articleName;
    }
}
