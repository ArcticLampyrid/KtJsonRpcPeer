package com.github.arcticlampyrid.ktjsonrpcpeer

public interface RpcCodec {
    public fun encodeMessage(msg: RpcMessage): ByteArray
    public fun decodeMessage(msg: ByteArray): RpcMessage
}