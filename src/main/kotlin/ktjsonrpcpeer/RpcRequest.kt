package ktjsonrpcpeer

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

internal open class RpcRequest(
        @SerializedName("json-rpc")
        val version: String?,
        val method: String,
        val params: JsonElement
)