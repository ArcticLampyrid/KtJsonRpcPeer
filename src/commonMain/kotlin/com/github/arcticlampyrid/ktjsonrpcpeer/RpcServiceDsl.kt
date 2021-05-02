package com.github.arcticlampyrid.ktjsonrpcpeer

import kotlinx.serialization.json.*

public class RpcServiceDsl internal constructor() {
    internal val method = HashMap<String, suspend (params: JsonElement) -> JsonElement>()

    public inline fun <reified TResult, reified TArgs> register(
        name: String,
        noinline processor: suspend (params: TArgs) -> TResult
    ) {
        registerLowLevel(name) {
            val p = if (TArgs::class == Unit::class) {
                Unit as TArgs
            } else {
                Json.decodeFromJsonElement<TArgs>(it)
            }
            val r = processor(p)
            if (TResult::class == Unit::class) {
                JsonNull
            } else {
                Json.encodeToJsonElement(r)
            }
        }
    }

    public fun registerLowLevel(name: String, processor: suspend (params: JsonElement) -> JsonElement) {
        method[name] = processor
    }
}
