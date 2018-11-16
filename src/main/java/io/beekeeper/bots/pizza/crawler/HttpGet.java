package io.beekeeper.bots.pizza.crawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class HttpGet {

    public static class HttpGetResponse {
        private final String response;
        private final Map<String, List<String>> headers;

        public HttpGetResponse(String response, Map<String, List<String>> headers) {
            this.response = response;
            this.headers = headers;
        }

        public String getResponse() {
            return response;
        }

        public Map<String, List<String>> getHeaders() {
            return headers;
        }
    }

    public static HttpGetResponse get(String stringUrl) throws IOException {
        return get(stringUrl, null);
    }

    public static HttpGetResponse get(String stringUrl, Map<String, String> headers) throws IOException {
        StringBuilder result = new StringBuilder();
        URL url = new URL(stringUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        if (headers != null) {
            headers.forEach(conn::addRequestProperty);
        }
        conn.setRequestMethod("GET");

        Map<String, List<String>> responseHeaders = conn.getHeaderFields();

        try (BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line).append('\n');
            }
        }
        return new HttpGetResponse(result.toString(), responseHeaders);
    }
}
