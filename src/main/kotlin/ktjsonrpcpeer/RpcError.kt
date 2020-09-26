package ktjsonrpcpeer

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
public data class RpcError(
        val code: Int,
        val message: String,
        val data: JsonElement? = null
) {
    public companion object {
        @JvmStatic
        public val InvalidRequest: RpcError = RpcError(-32600, "Invalid Request")

        @JvmStatic
        public val MethodNotFound: RpcError = RpcError(-32601, "Method not found")

        @JvmStatic
        public val InvalidParams: RpcError = RpcError(-32602, "Invalid params")

        @JvmStatic
        public val InternalError: RpcError = RpcError(-32603, "Internal error")

        @JvmStatic
        public val ParseError: RpcError = RpcError(-32700, "Parse error")
    }
}