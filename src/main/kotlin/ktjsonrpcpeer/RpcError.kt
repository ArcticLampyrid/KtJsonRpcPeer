package ktjsonrpcpeer

import com.google.gson.JsonElement

data class RpcError(
        val code: Int,
        val message: String,
        val data: JsonElement? = null
) {
    companion object {
        @JvmStatic
        val InvalidRequest = RpcError(-32600, "Invalid Request")

        @JvmStatic
        val MothedNotFound = RpcError(-32601, "Method not found")

        @JvmStatic
        val InvalidParams = RpcError(-32602, "Invalid params")

        @JvmStatic
        val InternalError = RpcError(-32603, "Internal error")

        @JvmStatic
        val ParseError = RpcError(-32700, "Parse error")
    }
}