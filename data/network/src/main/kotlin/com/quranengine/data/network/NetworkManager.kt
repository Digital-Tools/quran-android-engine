package com.quranengine.data.network

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

class NetworkManager(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun request(path: String, parameters: List<Pair<String, String>> = emptyList()): ByteArray {
        return try {
            val normalizedPath = path.trimStart('/')
            val response = client.get("$baseUrl/$normalizedPath") {
                parameters.forEach { (key, value) ->
                    parameter(key, value)
                }
            }
            response.bodyAsBytes()
        } catch (e: Exception) {
            throw NetworkError.from(e)
        }
    }
}
