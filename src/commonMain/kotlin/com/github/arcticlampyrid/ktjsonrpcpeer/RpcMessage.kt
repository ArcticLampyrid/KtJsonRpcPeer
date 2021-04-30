package com.github.arcticlampyrid.ktjsonrpcpeer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

internal sealed class RpcMessage {
    abstract val version: String?
}

internal sealed class RpcRequest : RpcMessage() {
    abstract val method: String
    abstract val params: JsonElement
}

@Serializable
internal class RpcNotifyRequest(
    @SerialName("jsonrpc")
    override val version: String?,
    override val method: String,
    override val params: JsonElement,
) : RpcRequest()

@Serializable
internal class RpcCallRequest(
    @SerialName("jsonrpc")
    override val version: String?,
    override val method: String,
    override val params: JsonElement,
    val id: JsonElement
) : RpcRequest()

internal sealed class RpcResponse : RpcMessage() {
    abstract val id: JsonElement
}

@Serializable
internal class RpcResultResponse(
    @SerialName("jsonrpc")
    override val version: String?,
    override val id: JsonElement,
    val result: JsonElement
) : RpcResponse() {
    constructor(id: JsonElement, result: JsonElement) : this("2.0", id, result)
}

@Serializable
internal class RpcErrorResponse(
    @SerialName("jsonrpc")
    override val version: String?,
    override val id: JsonElement,
    val error: RpcError
) : RpcResponse() {
    constructor(id: JsonElement, error: RpcError) : this("2.0", id, error)
}