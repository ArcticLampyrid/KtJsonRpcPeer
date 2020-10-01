package twitter.qiqiworld1.ktjsonrpcpeer

import kotlinx.serialization.json.JsonElement

internal abstract class RpcResponse : RpcMessage() {
    abstract val id: JsonElement
}