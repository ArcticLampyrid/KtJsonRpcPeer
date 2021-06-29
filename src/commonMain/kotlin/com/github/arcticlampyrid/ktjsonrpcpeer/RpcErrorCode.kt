package com.github.arcticlampyrid.ktjsonrpcpeer

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmStatic

@JvmInline
@Serializable
public value class RpcErrorCode(public val code: Int) {
    public companion object {
        @JvmStatic
        public val InvalidRequest: RpcErrorCode = RpcErrorCode(-32600)

        @JvmStatic
        public val MethodNotFound: RpcErrorCode = RpcErrorCode(-32601)

        @JvmStatic
        public val InvalidParams: RpcErrorCode = RpcErrorCode(-32602)

        @JvmStatic
        public val InternalError: RpcErrorCode = RpcErrorCode(-32603)

        @JvmStatic
        public val ParseError: RpcErrorCode = RpcErrorCode(-32700)
    }
}