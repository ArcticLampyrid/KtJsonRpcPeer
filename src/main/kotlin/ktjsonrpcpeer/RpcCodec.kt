package ktjsonrpcpeer

import kotlinx.serialization.json.JsonElement

interface RpcCodec {
    fun encodeMessage(msg: JsonElement): ByteArray
    fun decodeMessage(msg: ByteArray): JsonElement
}