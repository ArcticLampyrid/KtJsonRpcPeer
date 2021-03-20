package com.github.arcticlampyrid.ktjsonrpcpeer

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*
import kotlin.test.Test
import kotlin.test.assertEquals

class RpcBasicTest {
    @Test
    fun aSimpleCall() {
        runBlocking {
            val requestChannel = Channel<ByteArray>(5)
            val responseChannel = Channel<ByteArray>(5)
            val serverAdapter = RpcChannelAdapter(requestChannel, responseChannel)
            val clientAdapter = RpcChannelAdapter(responseChannel, requestChannel)
            val server = RpcChannel(serverAdapter)
            server.register<String, JsonArray>("test") {
                "hello" + it[0].jsonPrimitive.content
            }
            val client = RpcChannel(clientAdapter)
            val result: String = client.call("test", arrayOf("123"))
            assertEquals("hello123", result)
        }
    }

    @Test
    fun anyCanBeCastToUnit() {
        runBlocking {
            val requestChannel = Channel<ByteArray>(5)
            val responseChannel = Channel<ByteArray>(5)
            val serverAdapter = RpcChannelAdapter(requestChannel, responseChannel)
            val clientAdapter = RpcChannelAdapter(responseChannel, requestChannel)
            val server = RpcChannel(serverAdapter)
            server.register<String, Unit>("test") {
                "hello"
            }
            val client = RpcChannel(clientAdapter)
            client.call<Unit, JsonArray>("test", buildJsonArray {
                add(JsonNull)
            })
        }
    }

    @Test
    fun unitSerializedToNull() {
        runBlocking {
            val requestChannel = Channel<ByteArray>(5)
            val responseChannel = Channel<ByteArray>(5)
            val serverAdapter = RpcChannelAdapter(requestChannel, responseChannel)
            val clientAdapter = RpcChannelAdapter(responseChannel, requestChannel)
            val server = RpcChannel(serverAdapter)
            server.register<Unit, JsonElement>("test") {

            }
            val client = RpcChannel(clientAdapter)
            val r = client.call<JsonElement, JsonArray>("test", buildJsonArray {
                add(JsonNull)
            })
            assertEquals(JsonNull, r)
        }
    }

    @Test
    fun waitUntilConnectClosed() {
        runBlocking {
            val requestChannel = Channel<ByteArray>(5)
            val responseChannel = Channel<ByteArray>(5)
            val adapter = RpcChannelAdapter(requestChannel, responseChannel)
            val server = RpcChannel(adapter)
            requestChannel.close()
            responseChannel.close()
            server.join()
        }
    }
}