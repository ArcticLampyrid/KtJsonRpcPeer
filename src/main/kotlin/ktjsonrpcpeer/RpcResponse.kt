package ktjsonrpcpeer

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

internal open class RpcResponse(
        @SerializedName("json-rpc")
        val version: String?,
        val id: JsonElement?
)