package com.github.arcticlampyrid.ktjsonrpcpeer

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmStatic

@JvmInline
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

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = RpcErrorCode::class)
internal object RpcErrorCodeSerializer : KSerializer<RpcErrorCode> {
    override fun serialize(encoder: Encoder, value: RpcErrorCode): Unit = encoder.encodeInt(value.code)
    override fun deserialize(decoder: Decoder): RpcErrorCode = RpcErrorCode(decoder.decodeInt())

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor =
        buildSerialDescriptor("RpcErrorCode", PolymorphicKind.SEALED)
}