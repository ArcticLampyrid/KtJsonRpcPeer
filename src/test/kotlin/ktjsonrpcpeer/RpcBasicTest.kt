package ktjsonrpcpeer

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonPrimitive
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
}