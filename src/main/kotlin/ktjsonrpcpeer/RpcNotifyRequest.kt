package ktjsonrpcpeer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
internal class RpcNotifyRequest(
        @SerialName("json-rpc")
        override val version: String?,
        override val method: String,
        override val params: JsonElement,
) : RpcRequest()