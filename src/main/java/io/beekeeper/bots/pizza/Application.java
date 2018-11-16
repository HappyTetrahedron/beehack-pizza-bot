package io.beekeeper.bots.pizza;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class Application {
    private final static String BASE_URL = "https://pizza.dev.beekeeper.io";
    private final static String API_TOKEN = "2c39a8b4-4af7-44df-abe0-236d9de0ab27";

    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        PizzaBot bot = new PizzaBot(BASE_URL, API_TOKEN);
        bot.start();
    }

}
