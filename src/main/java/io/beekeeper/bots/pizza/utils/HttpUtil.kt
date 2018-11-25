package io.beekeeper.bots.pizza.utils

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object HttpUtil {

    data class HttpGetResponse(val response: String, val headers: Map<String, List<String>>)

    fun doGet(stringUrl: String, headers: Map<String, String>? = null): HttpGetResponse {
        val result = StringBuilder()
        val url = URL(stringUrl)
        val conn = url.openConnection() as HttpURLConnection
        headers?.forEach { key, value ->
            conn.addRequestProperty(key, value)
        }
        conn.requestMethod = "GET"

        val responseHeaders = conn.headerFields

        BufferedReader(InputStreamReader(conn.inputStream))
                .use { reader ->
                    while (true) {
                        val line = reader.readLine() ?: break
                        result.append(line).append('\n')
                    }
                }
        return HttpGetResponse(result.toString(), responseHeaders)
    }
}
