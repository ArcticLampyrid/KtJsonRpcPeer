package com.github.arcticlampyrid.ktjsonrpcpeer

import kotlinx.serialization.json.Json


public object RpcJsonCodec : RpcCodec {
    override fun encodeMessage(msg: RpcMessage): ByteArray {
        return Json.encodeToString(RpcMessageSerializer, msg).encodeToByteArray()
    }

    override fun decodeMessage(msg: ByteArray): RpcMessage {
        return Json.decodeFromString(RpcMessageSerializer, msg.decodeToString())
    }
}