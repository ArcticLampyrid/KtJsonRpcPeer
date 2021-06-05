package com.github.arcticlampyrid.ktjsonrpcpeer

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
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
        val resultSerializer =
            if (TResult::class == Unit::class) null
            else serializer<TResult>()
        val argDeserializer =
            if (TArgs::class == Unit::class) null
            else serializer<TArgs>()
        register(name, resultSerializer, argDeserializer, processor)
    }

    @Suppress("NOTHING_TO_INLINE")
    public inline fun <TResult, TArgs> register(
        name: String,
        resultSerializer: SerializationStrategy<TResult>?,
        argDeserializer: DeserializationStrategy<TArgs>?,
        noinline processor: suspend (params: TArgs) -> TResult
    ) {
        registerLowLevel(name) {
            @Suppress("UNCHECKED_CAST")
            val p = argDeserializer?.let { s ->
                Json.decodeFromJsonElement(s, it)
            } ?: Unit as TArgs
            val result = processor(p)
            resultSerializer?.let { s ->
                Json.encodeToJsonElement(s, result)
            } ?: JsonNull
        }
    }

    public fun registerLowLevel(name: String, processor: suspend (params: JsonElement) -> JsonElement) {
        method[name] = processor
    }
}
