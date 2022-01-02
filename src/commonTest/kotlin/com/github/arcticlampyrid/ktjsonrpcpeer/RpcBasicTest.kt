package com.github.arcticlampyrid.ktjsonrpcpeer

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.*
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class RpcBasicTest {
    @Test
    fun aSimpleCall(): TestResult = runTest {
        val requestChannel = Channel<ByteArray>(5)
        val responseChannel = Channel<ByteArray>(5)
        val serverAdapter = RpcChannelAdapter(requestChannel, responseChannel)
        val clientAdapter = RpcChannelAdapter(responseChannel, requestChannel)

        @Suppress("UNUSED_VARIABLE")
        val server = RpcChannel(serverAdapter) {
            register<String, JsonArray>("test") {
                "hello" + it[0].jsonPrimitive.content
            }
        }
        val client = RpcChannel(clientAdapter)
        val result: String = client.call("test", arrayOf("123"))
        assertEquals("hello123", result)
    }


    @Test
    fun anyCanBeCastToUnit(): TestResult = runTest {
        val requestChannel = Channel<ByteArray>(5)
        val responseChannel = Channel<ByteArray>(5)
        val serverAdapter = RpcChannelAdapter(requestChannel, responseChannel)
        val clientAdapter = RpcChannelAdapter(responseChannel, requestChannel)

        @Suppress("UNUSED_VARIABLE")
        val server = RpcChannel(serverAdapter) {
            register<String, Unit>("test") {
                "hello"
            }
        }
        val client = RpcChannel(clientAdapter)
        client.call<Unit, JsonArray>("test", buildJsonArray {
            add(JsonNull)
        })
    }


    @Test
    fun unitSerializedToNull(): TestResult = runTest {
        val requestChannel = Channel<ByteArray>(5)
        val responseChannel = Channel<ByteArray>(5)
        val serverAdapter = RpcChannelAdapter(requestChannel, responseChannel)
        val clientAdapter = RpcChannelAdapter(responseChannel, requestChannel)

        @Suppress("UNUSED_VARIABLE")
        val server = RpcChannel(serverAdapter) {
            register<Unit, JsonElement>("test") {

            }
        }
        val client = RpcChannel(clientAdapter)
        val r = client.call<JsonElement, JsonArray>("test", buildJsonArray {
            add(JsonNull)
        })
        assertEquals(JsonNull, r)
    }


    @Test
    fun waitUntilConnectClosed(): TestResult = runTest {
        val requestChannel = Channel<ByteArray>(5)
        val responseChannel = Channel<ByteArray>(5)
        val adapter = RpcChannelAdapter(requestChannel, responseChannel)
        val server = RpcChannel(adapter)
        requestChannel.close()
        responseChannel.close()
        server.join()
    }

}
