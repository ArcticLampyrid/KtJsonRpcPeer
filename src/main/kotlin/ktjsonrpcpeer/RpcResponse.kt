package ktjsonrpcpeer

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class RpcResponse(
        @SerializedName("json-rpc")
        val version: String?,
        val id: JsonElement,
        val result: JsonElement?,
        val error: RpcError? = null
) {
    constructor(id: JsonElement, result: JsonElement?) : this("2.0", id, result, null)
    constructor(id: JsonElement, error: RpcError?) : this("2.0", id, null, error)
}