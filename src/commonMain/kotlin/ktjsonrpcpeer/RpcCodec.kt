package ktjsonrpcpeer

import kotlinx.serialization.json.JsonElement

public interface RpcCodec {
    public fun encodeMessage(msg: JsonElement): ByteArray
    public fun decodeMessage(msg: ByteArray): JsonElement
}