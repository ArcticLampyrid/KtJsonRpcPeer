package ktjsonrpcpeer

import kotlinx.serialization.json.JsonElement

internal abstract class RpcRequest : RpcMessage() {
    abstract val method: String
    abstract val params: JsonElement
}
