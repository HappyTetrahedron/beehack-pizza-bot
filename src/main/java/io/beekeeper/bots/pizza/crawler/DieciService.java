package io.beekeeper.bots.pizza.crawler;

import com.google.gson.*;

import java.io.IOException;
import java.util.*;

public class DieciService {

    public static final String DIECI_MENU_BASE_URL = "https://webshop.dieci.ch/c/";
    public static final String DIECI_INIT_URL = "https://webshop.dieci.ch/store/Z%C3%BCrich%20links%20der%20Limmat";

    public static final String[] DIECI_PAGES = {"1/pizza", "2/pasta"};


    private Map<String, String> headers;

    public void initializeSession() throws IOException {
        HttpGet.HttpGetResponse response = HttpGet.get(DIECI_INIT_URL);
        List<String> cookies = response.getHeaders().get("Set-Cookie");

        this.headers = new HashMap<>();
        for (String cookie : cookies) {
            headers.put("Cookie", cookie.split(";", 2)[0]);
        }
    }

    public List<DieciMenuItem> fetchAllDieciPages() throws IOException {
        List<DieciMenuItem> result = new ArrayList<>();

        for (String page : DIECI_PAGES) {
            List<DieciMenuItem> pageItems = fetchDieciPage(page);
            result.addAll(pageItems);
        }

        return result;
    }

    public List<DieciMenuItem> fetchDieciPage(String page) throws IOException {
        if (this.headers == null) {
            throw new IllegalStateException();
        }

        System.out.println("Crawling Dieci page: " + DIECI_MENU_BASE_URL + page);
        HttpGet.HttpGetResponse response = HttpGet.get(DIECI_MENU_BASE_URL + page, this.headers);
        return parsePage(response.getResponse());
    }

    public List<DieciMenuItem> parsePage(String htmlPage) {
        String[] lines = htmlPage.split("\n");

        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("var articles = {")) {
                return parseArticles(line);
            }
        }

        return Collections.emptyList();
    }

    private List<DieciMenuItem> parseArticles(String line) {
        String json = line.substring(line.indexOf('{'), line.length() - 1);

        List<DieciMenuItem> items = new ArrayList<>();
        JsonParser parser = new JsonParser();
        JsonObject element = parser.parse(json).getAsJsonObject();

        for (Map.Entry<String, JsonElement> jsonElementEntry : element.entrySet()) {
            String jsonItem = jsonElementEntry.getValue().getAsJsonObject().toString();
            DieciMenuItem dieciMenuItem = new Gson().fromJson(jsonItem, DieciMenuItem.class);
            items.add(dieciMenuItem);
        }

        return items;
    }

}
