package ktjsonrpcpeer

import com.google.gson.JsonElement

internal class RpcErrorResponse(
        version: String?,
        id: JsonElement?,
        val error: RpcError
) : RpcResponse(version, id) {
    constructor(id: JsonElement?, error: RpcError) : this("2.0", id, error)
}