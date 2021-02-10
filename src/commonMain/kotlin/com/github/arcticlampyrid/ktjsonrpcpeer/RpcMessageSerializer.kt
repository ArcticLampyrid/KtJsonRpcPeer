package com.github.arcticlampyrid.ktjsonrpcpeer

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject

internal object RpcMessageSerializer : JsonContentPolymorphicSerializer<RpcMessage>(RpcMessage::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out RpcMessage> {
        if (element !is JsonObject) {
            throw IllegalArgumentException()
        }
        return when {
            "method" in element -> when {
                "id" in element -> RpcCallRequest.serializer()
                else -> RpcNotifyRequest.serializer()
            }
            else -> when {
                "error" in element && element["error"] !is JsonNull -> RpcErrorResponse.serializer()
                "result" in element -> RpcResultResponse.serializer()
                else -> throw IllegalArgumentException()
            }
        }
    }
}