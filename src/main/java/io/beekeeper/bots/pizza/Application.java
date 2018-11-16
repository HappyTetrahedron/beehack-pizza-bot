package io.beekeeper.bots.pizza;

import java.util.HashMap;
import io.beekeeper.bots.pizza.crawler.DieciMenuItem;
import io.beekeeper.bots.pizza.crawler.DieciService;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Application {
    private final static String BASE_URL = "https://pizza.dev.beekeeper.io";
    private final static String API_TOKEN = "2c39a8b4-4af7-44df-abe0-236d9de0ab27";

    public static void main(String[] args) throws IOException {
        PizzaBot bot = new PizzaBot(BASE_URL, API_TOKEN);


        Parser<DieciMenuItem> dieciMenuItemParser = initDieciMenuParser();
        bot.setParser(dieciMenuItemParser);

        bot.start();
    }

    private static Parser<DieciMenuItem> initDieciMenuParser() throws IOException {
        DieciService dieci = new DieciService();
        dieci.initializeSession();
        List<DieciMenuItem> result = dieci.fetchAllDieciPages();
        System.out.println(result.size() + " menu items found");
        Map<String, DieciMenuItem> mapItems = result.stream().collect(Collectors.toMap(
                DieciMenuItem::getArticleName,
                item -> item
        ));

        return new Parser<>(mapItems);
    }

}
