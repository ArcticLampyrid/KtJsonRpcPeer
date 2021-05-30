package com.github.arcticlampyrid.ktjsonrpcpeer

import io.ktor.client.*
import io.ktor.client.features.websocket.*
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertContentEquals

class KtorWebSocketAdapterTest {
    @Test
    fun echoTest() {
        runBlocking {
            HttpClient {
                install(WebSockets)
            }.use { httpClient ->
                httpClient.webSocket("wss://echo.websocket.org/") {
                    val adapter = RpcKtorWebSocketAdapter(this)
                    val data = byteArrayOf(1, 2, 3, 4, 5, 6)
                    adapter.writeMessage(data)
                    assertContentEquals(data, adapter.readMessage())
                }
            }
        }
    }
}