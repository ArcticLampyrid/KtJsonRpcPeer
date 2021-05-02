package com.github.arcticlampyrid.ktjsonrpcpeer

import kotlinx.serialization.json.JsonElement
import kotlin.jvm.JvmStatic

public class RpcService internal constructor(
    internal val method: Map<String, suspend (params: JsonElement) -> JsonElement>
){
    public companion object {
        @JvmStatic
        public val Empty : RpcService = RpcService(emptyMap())
    }
}
