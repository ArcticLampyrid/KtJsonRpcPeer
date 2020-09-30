package ktjsonrpcpeer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
internal class RpcResultResponse(
    @SerialName("jsonrpc")
    override val version: String?,
    override val id: JsonElement,
    val result: JsonElement
) : RpcResponse() {
    constructor(id: JsonElement, result: JsonElement) : this("2.0", id, result)
}