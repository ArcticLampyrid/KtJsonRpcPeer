package ktjsonrpcpeer

import com.google.gson.JsonElement

interface RpcCodec {
    fun encodeMessage(msg: JsonElement): ByteArray
    fun decodeMessage(msg: ByteArray): JsonElement
}