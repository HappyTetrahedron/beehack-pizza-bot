package io.beekeeper.bots.pizza;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.beekeeper.bots.pizza.crawler.DieciMenuItem;
import io.beekeeper.bots.pizza.crawler.DieciService;

public class Application {
    private final static String BASE_URL = "https://team.beekeeper.io";
    private final static String API_TOKEN = "975f909c-7344-4123-89d8-d81b63dc641b";

    public static void main(String[] args) throws IOException {
        PizzaBot bot = new PizzaBot(BASE_URL, API_TOKEN);


        Parser<DieciMenuItem> dieciMenuItemParser = initDieciMenuParser();
        bot.setParser(dieciMenuItemParser);

        System.out.println("Starting bot");
        bot.start();
    }

    private static Parser<DieciMenuItem> initDieciMenuParser() throws IOException {
        DieciService dieci = new DieciService();
        dieci.initializeSession();
        List<DieciMenuItem> result = dieci.fetchAllDieciPages();
        System.out.println(result.size() + " menu items found");
        Map<String, DieciMenuItem> mapItems = result.stream().collect(Collectors.toMap(
                DieciMenuItem::getKey,
                item -> item
        ));

        return new Parser<>(mapItems);
    }

}
