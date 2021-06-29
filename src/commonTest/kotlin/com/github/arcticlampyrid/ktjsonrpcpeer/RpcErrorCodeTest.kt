package com.github.arcticlampyrid.ktjsonrpcpeer

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class RpcErrorCodeTest {
    @Test
    fun serializeRpcErrorCode() {
        assertEquals("12345", Json.encodeToString(RpcErrorCode(12345)))
    }

    @Test
    fun serializeRpcErrorStruct() {
        assertEquals("""{"code":12345,"message":""}""", Json.encodeToString(RpcError(RpcErrorCode(12345), "")))
    }
}