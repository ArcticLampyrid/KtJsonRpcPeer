package ktjsonrpcpeer

import com.google.gson.JsonElement

internal class RpcCallRequest(
        version: String?,
        method: String,
        params: JsonElement,
        val id: JsonElement?
) : RpcRequest(version, method, params)