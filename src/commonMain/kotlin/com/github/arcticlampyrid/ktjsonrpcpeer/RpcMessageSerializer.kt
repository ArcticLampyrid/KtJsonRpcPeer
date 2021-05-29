@file:OptIn(ExperimentalSerializationApi::class, RpcInternalModelApi::class)

package com.github.arcticlampyrid.ktjsonrpcpeer

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import kotlin.jvm.JvmStatic

internal object RpcSingleMessageSerializer : KSerializer<RpcSingleMessage> {
    private fun selectDeserializer(element: JsonElement): DeserializationStrategy<out RpcSingleMessage> {
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

    internal fun deserializeTree(json: Json, tree: JsonElement): RpcSingleMessage {
        @Suppress("UNCHECKED_CAST")
        val actualSerializer = selectDeserializer(tree) as DeserializationStrategy<RpcSingleMessage>
        return json.decodeFromJsonElement(actualSerializer, tree)
    }

    override fun deserialize(decoder: Decoder): RpcSingleMessage {
        val input = decoder as JsonDecoder
        val tree = input.decodeJsonElement()
        return deserializeTree(input.json, tree)
    }

    @InternalSerializationApi
    override val descriptor: SerialDescriptor =
        buildSerialDescriptor("RpcSingleMessageSerializer", PolymorphicKind.SEALED)

    override fun serialize(encoder: Encoder, value: RpcSingleMessage) {
        val actualSerializer = when (value) {
            is RpcCallRequest -> RpcCallRequest.serializer()
            is RpcNotifyRequest -> RpcNotifyRequest.serializer()
            is RpcErrorResponse -> RpcErrorResponse.serializer()
            is RpcResultResponse -> RpcResultResponse.serializer()
        }
        @Suppress("UNCHECKED_CAST")
        (actualSerializer as KSerializer<RpcSingleMessage>).serialize(encoder, value)
    }
}

@OptIn(ExperimentalSerializationApi::class)
internal object RpcMessageSerializer : KSerializer<RpcMessage> {
    @JvmStatic
    private val RpcSingleMessageListSerializer = ListSerializer(RpcSingleMessageSerializer)

    override fun deserialize(decoder: Decoder): RpcMessage {
        val input = decoder as JsonDecoder
        return when (val tree = input.decodeJsonElement()) {
            is JsonArray -> RpcBatchMessage(input.json.decodeFromJsonElement(RpcSingleMessageListSerializer, tree))
            else -> RpcSingleMessageSerializer.deserializeTree(input.json, tree)
        }
    }

    @InternalSerializationApi
    override val descriptor: SerialDescriptor =
        buildSerialDescriptor("RpcMessageSerializer", PolymorphicKind.SEALED)

    override fun serialize(encoder: Encoder, value: RpcMessage) =
        when (value) {
            is RpcSingleMessage -> RpcSingleMessageSerializer.serialize(encoder, value)
            is RpcBatchMessage -> RpcSingleMessageListSerializer.serialize(encoder, value.content)
        }
}