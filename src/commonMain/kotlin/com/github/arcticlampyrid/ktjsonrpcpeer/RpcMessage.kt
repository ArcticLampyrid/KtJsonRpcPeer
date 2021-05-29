package com.github.arcticlampyrid.ktjsonrpcpeer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

public sealed class RpcMessage

public class RpcBatchMessage(
    public val content: List<RpcSingleMessage>
) : RpcMessage()

/**
 * [RpcSingleMessage] type is stable, but what property it has is unstable.
 */
public sealed class RpcSingleMessage : RpcMessage() {
    @RpcInternalModelApi
    public abstract val version: String?
}

/**
 * [RpcRequest] type is stable, but what property or subclass it has is unstable.
 */
public sealed class RpcRequest : RpcSingleMessage() {
    @RpcInternalModelApi
    public abstract val method: String

    @RpcInternalModelApi
    public abstract val params: JsonElement
}

@Serializable
@RpcInternalModelApi
public class RpcNotifyRequest(
    @SerialName("jsonrpc")
    override val version: String?,
    override val method: String,
    override val params: JsonElement,
) : RpcRequest()

@Serializable
@RpcInternalModelApi
public class RpcCallRequest(
    @SerialName("jsonrpc")
    override val version: String?,
    override val method: String,
    override val params: JsonElement,
    public val id: JsonElement
) : RpcRequest()


/**
 * [RpcResponse] type is stable, but what property or subclass it has is unstable.
 */
public sealed class RpcResponse : RpcSingleMessage() {
    @RpcInternalModelApi
    public abstract val id: JsonElement
}

@Serializable
@RpcInternalModelApi
public class RpcResultResponse(
    @SerialName("jsonrpc")
    override val version: String?,
    override val id: JsonElement,
    public val result: JsonElement
) : RpcResponse() {
    public constructor(id: JsonElement, result: JsonElement) : this("2.0", id, result)
}

@Serializable
@RpcInternalModelApi
public class RpcErrorResponse(
    @SerialName("jsonrpc")
    override val version: String?,
    override val id: JsonElement,
    public val error: RpcError
) : RpcResponse() {
    public constructor(id: JsonElement, error: RpcError) : this("2.0", id, error)
}