package com.github.arcticlampyrid.ktjsonrpcpeer

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject

@OptIn(ExperimentalSerializationApi::class)
internal object RpcMessageSerializer : KSerializer<RpcMessage> {
    private fun selectDeserializer(element: JsonElement): DeserializationStrategy<out RpcMessage> {
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

    override fun deserialize(decoder: Decoder): RpcMessage {
        val input = decoder as JsonDecoder
        val tree = input.decodeJsonElement()
        @Suppress("UNCHECKED_CAST")
        val actualSerializer = selectDeserializer(tree) as DeserializationStrategy<RpcMessage>
        return input.json.decodeFromJsonElement(actualSerializer, tree)
    }

    @InternalSerializationApi
    override val descriptor: SerialDescriptor =
        buildSerialDescriptor("RpcMessageSerializer", PolymorphicKind.SEALED)

    override fun serialize(encoder: Encoder, value: RpcMessage) {
        val actualSerializer = when (value) {
            is RpcCallRequest -> RpcCallRequest.serializer()
            is RpcNotifyRequest -> RpcNotifyRequest.serializer()
            is RpcErrorResponse -> RpcErrorResponse.serializer()
            is RpcResultResponse -> RpcResultResponse.serializer()
        }
        @Suppress("UNCHECKED_CAST")
        (actualSerializer as KSerializer<RpcMessage>).serialize(encoder, value)
    }
}