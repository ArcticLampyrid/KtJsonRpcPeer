package ktjsonrpcpeer

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class RpcRequest(
        @SerializedName("json-rpc")
        val version: String?,
        val id: JsonElement?,
        val method: String,
        val params: JsonElement
)