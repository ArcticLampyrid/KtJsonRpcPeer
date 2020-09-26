package ktjsonrpcpeer

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
internal class RpcNotifyRequest(
        override val version: String?,
        override val method: String,
        override val params: JsonElement,
) : RpcRequest()