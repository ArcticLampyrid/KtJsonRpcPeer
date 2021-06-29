package com.github.arcticlampyrid.ktjsonrpcpeer

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlin.jvm.JvmStatic

@Serializable
public data class RpcError(
    val code: RpcErrorCode,
    val message: String,
    val data: JsonElement? = null
) {
    public companion object {
        @JvmStatic
        public val InvalidRequest: RpcError = RpcError(RpcErrorCode.InvalidRequest, "Invalid Request")

        @JvmStatic
        public val MethodNotFound: RpcError = RpcError(RpcErrorCode.MethodNotFound, "Method not found")

        @JvmStatic
        public val InvalidParams: RpcError = RpcError(RpcErrorCode.InvalidParams, "Invalid params")

        @JvmStatic
        public val InternalError: RpcError = RpcError(RpcErrorCode.InternalError, "Internal error")

        @JvmStatic
        public val ParseError: RpcError = RpcError(RpcErrorCode.ParseError, "Parse error")
    }
}