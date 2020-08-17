package ktjsonrpcpeer

import com.google.gson.JsonElement

internal class RpcResultResponse(
        version: String?,
        id: JsonElement?,
        val result: JsonElement
) : RpcResponse(version, id) {
    constructor(id: JsonElement?, result: JsonElement) : this("2.0", id, result)
}