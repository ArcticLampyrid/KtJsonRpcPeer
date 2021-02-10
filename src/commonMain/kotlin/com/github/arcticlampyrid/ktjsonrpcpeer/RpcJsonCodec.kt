package com.github.arcticlampyrid.ktjsonrpcpeer

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement


public object RpcJsonCodec : RpcCodec {
    override fun encodeMessage(msg: JsonElement): ByteArray {
        return Json.encodeToString(msg).encodeToByteArray()
    }

    override fun decodeMessage(msg: ByteArray): JsonElement {
        return Json.decodeFromString(msg.decodeToString())
    }
}