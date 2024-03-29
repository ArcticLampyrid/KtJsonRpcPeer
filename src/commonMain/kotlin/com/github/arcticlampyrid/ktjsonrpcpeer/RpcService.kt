package com.github.arcticlampyrid.ktjsonrpcpeer

import kotlinx.serialization.json.JsonElement
import kotlin.jvm.JvmStatic

private val UNHANDLED_ERROR_CODE = RpcErrorCode(-32000)

@OptIn(RpcInternalModelApi::class)
public class RpcService internal constructor(
    private val method: Map<String, suspend (params: JsonElement) -> JsonElement>
) {
    public companion object {
        @JvmStatic
        public val Empty: RpcService = RpcService(emptyMap())
    }

    public suspend fun handleRequest(msg: RpcRequest): RpcResponse? {
        return when (msg) {
            is RpcNotifyRequest -> {
                method[msg.method]?.let { processor ->
                    processor(msg.params)
                }
                null
            }
            is RpcCallRequest -> {
                method[msg.method]?.let { processor ->
                    try {
                        RpcResultResponse(msg.id, processor(msg.params))
                    } catch (e: RpcTargetException) {
                        RpcErrorResponse(msg.id, e.info)
                    } catch (e: Throwable) {
                        RpcErrorResponse(msg.id, RpcError(UNHANDLED_ERROR_CODE, e.message ?: "Unknown error"))
                    }
                } ?: RpcErrorResponse(msg.id, RpcError.MethodNotFound)
            }
        }
    }
}
