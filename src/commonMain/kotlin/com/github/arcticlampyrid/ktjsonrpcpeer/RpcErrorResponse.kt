package com.github.arcticlampyrid.ktjsonrpcpeer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
internal class RpcErrorResponse(
    @SerialName("jsonrpc")
    override val version: String?,
    override val id: JsonElement,
    val error: RpcError
) : RpcResponse() {
    constructor(id: JsonElement, error: RpcError) : this("2.0", id, error)
}