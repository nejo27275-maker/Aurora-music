package com.auroramusic.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class JamendoTrack(
    val id: String,
    val name: String,
    val artist: String,
    val album: String,
    val image: String,
    val audio: String,
    val durationSeconds: Int,
    val licenseUrl: String,
    val pageUrl: String
)

object JamendoCatalog {
    suspend fun search(clientId: String, query: String = "", limit: Int = 30): List<JamendoTrack> =
        withContext(Dispatchers.IO) {
            require(clientId.isNotBlank()) { "Informe o Client ID do Jamendo para ativar o catálogo." }

            val params = linkedMapOf(
                "client_id" to clientId.trim(),
                "format" to "json",
                "limit" to limit.coerceIn(1, 50).toString(),
                "audioformat" to "mp32",
                "imagesize" to "200",
                "type" to "single albumtrack",
                "include" to "licenses"
            )
            if (query.isBlank()) {
                params["order"] = "popularity_month_desc"
            } else {
                params["search"] = query.trim()
            }

            val queryString = params.entries.joinToString("&") { (key, value) ->
                "${encode(key)}=${encode(value)}"
            }
            val connection = (URL("https://api.jamendo.com/v3.0/tracks/?$queryString")
                .openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 15_000
                readTimeout = 20_000
                setRequestProperty("Accept", "application/json")
            }

            try {
                val code = connection.responseCode
                val stream = if (code in 200..299) connection.inputStream else connection.errorStream
                val body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
                if (code !in 200..299) {
                    throw IOException("O catálogo respondeu HTTP $code. Confira o Client ID e tente novamente.")
                }

                val root = JSONObject(body)
                val headers = root.optJSONObject("headers")
                if (headers?.optString("status") == "failed") {
                    val detail = headers.optString("error_message").takeIf { it.isNotBlank() }
                        ?: "Não foi possível consultar o catálogo."
                    throw IOException(detail)
                }

                val results = root.optJSONArray("results") ?: return@withContext emptyList()
                buildList {
                    for (index in 0 until results.length()) {
                        val item = results.optJSONObject(index) ?: continue
                        val audio = item.optString("audio")
                        if (!audio.startsWith("https://")) continue
                        add(
                            JamendoTrack(
                                id = item.optString("id", index.toString()),
                                name = item.optString("name").ifBlank { "Faixa sem título" },
                                artist = item.optString("artist_name").ifBlank { "Artista independente" },
                                album = item.optString("album_name"),
                                image = item.optString("image").ifBlank { item.optString("album_image") },
                                audio = audio,
                                durationSeconds = item.optInt("duration", 0),
                                licenseUrl = item.optString("license_ccurl"),
                                pageUrl = item.optString("shareurl")
                            )
                        )
                    }
                }
            } finally {
                connection.disconnect()
            }
        }

    private fun encode(value: String): String = URLEncoder.encode(value, Charsets.UTF_8.name())
}
