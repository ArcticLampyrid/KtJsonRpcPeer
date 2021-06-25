package com.github.arcticlampyrid.ktjsonrpcpeer

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.serializer
import kotlin.jvm.JvmName

public class RpcServiceDsl internal constructor() {
    internal val method = HashMap<String, suspend (params: JsonElement) -> JsonElement>()

    @JvmName("registerReified")
    public inline fun <reified TResult, reified TArgs> register(
        name: String,
        noinline processor: suspend (params: TArgs) -> TResult
    ) {
        val resultSerializer = serializer<TResult>()
        val argDeserializer = serializer<TArgs>()
        register(name, resultSerializer, argDeserializer, processor)
    }

    @Suppress("NOTHING_TO_INLINE")
    public inline fun <TResult, TArgs> register(
        name: String,
        resultSerializer: SerializationStrategy<TResult>,
        argDeserializer: DeserializationStrategy<TArgs>,
        noinline processor: suspend (params: TArgs) -> TResult
    ) {
        registerLowLevel(name) {
            @Suppress("UNCHECKED_CAST")
            val p = when (argDeserializer) {
                Unit.serializer() -> Unit as TArgs
                else -> RpcChannel.jsonIgnoringUnknownKeys.decodeFromJsonElement(argDeserializer, it)
            }
            val result = processor(p)
            when (resultSerializer) {
                Unit.serializer() -> JsonNull
                else -> Json.encodeToJsonElement(resultSerializer, result)
            }
        }
    }

    public fun registerLowLevel(name: String, processor: suspend (params: JsonElement) -> JsonElement) {
        method[name] = processor
    }
}
