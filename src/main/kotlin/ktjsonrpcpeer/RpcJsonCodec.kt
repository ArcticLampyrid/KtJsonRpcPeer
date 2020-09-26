package ktjsonrpcpeer

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement


public object RpcJsonCodec : RpcCodec {
    override fun encodeMessage(msg: JsonElement): ByteArray {
        return Json.encodeToString(msg).toByteArray()
    }

    override fun decodeMessage(msg: ByteArray): JsonElement {
        return Json.decodeFromString(msg.decodeToString())
    }
}