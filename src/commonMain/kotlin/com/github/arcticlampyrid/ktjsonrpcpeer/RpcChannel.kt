@file:OptIn(RpcInternalModelApi::class)

package com.github.arcticlampyrid.ktjsonrpcpeer

import com.github.arcticlampyrid.ktjsonrpcpeer.internal.PendingMap
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.serialization.json.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.jvm.JvmStatic

public class RpcChannel(
    private val adapter: RpcMessageAdapter,
    private val codec: RpcCodec = RpcJsonCodec,
    parentCoroutineContext: CoroutineContext = EmptyCoroutineContext,
    service: RpcService = RpcService.Empty
) : CoroutineScope {
    private val supervisor = SupervisorJob(parentCoroutineContext[Job])
    override val coroutineContext: CoroutineContext = parentCoroutineContext + supervisor

    private val pending = PendingMap<SendChannel<RpcResponse>>()
    private val _service = atomic(service)
    public var service: RpcService
        get() = _service.value
        set(value) {
            _service.value = value
        }

    public constructor(
        adapter: RpcMessageAdapter,
        codec: RpcCodec = RpcJsonCodec,
        parentCoroutineContext: CoroutineContext = EmptyCoroutineContext,
        serviceDefiner: RpcServiceDefiner
    ) : this(adapter, codec, parentCoroutineContext, serviceDefiner.build())

    init {
        this.launch {
            while (true) {
                val msg: ByteArray
                try {
                    msg = adapter.readMessage()
                } catch (e: Throwable) {
                    this@RpcChannel.cancel("rpc channel not available", e)
                    break
                }
                this@RpcChannel.launch {
                    feedData(msg)
                }
            }
        }
    }

    private suspend fun feedData(msg: ByteArray) {
        val root: RpcMessage
        try {
            root = codec.decodeMessage(msg)
        } catch (e: Exception) {
            adapter.writeMessage(
                codec.encodeMessage(
                    RpcErrorResponse(
                        JsonNull,
                        RpcError.ParseError
                    )
                )
            )
            return
        }
        val response = when (root) {
            is RpcBatchMessage ->
                root.content.mapNotNull {
                    handleMsg(it)
                }.takeUnless {
                    it.isEmpty()
                }?.let {
                    RpcBatchMessage(it)
                }
            is RpcSingleMessage ->
                handleMsg(root)
        }
        response?.let {
            adapter.writeMessage(
                codec.encodeMessage(it)
            )
        }
    }

    private suspend fun handleMsg(msg: RpcSingleMessage): RpcResponse? {
        when (msg) {
            is RpcNotifyRequest -> {
                val processor = _service.value.method[msg.method] ?: return null
                processor(msg.params)
                return null
            }
            is RpcCallRequest -> {
                val processor = _service.value.method[msg.method]
                    ?: return RpcErrorResponse(msg.id, RpcError.MethodNotFound)
                return try {
                    RpcResultResponse(msg.id, processor(msg.params))
                } catch (e: RpcTargetException) {
                    RpcErrorResponse(msg.id, e.info)
                } catch (e: Exception) {
                    RpcErrorResponse(msg.id, RpcError(-32000, e.message ?: "Unknown error"))
                }
            }
            is RpcResponse -> {
                val id = msg.id.jsonPrimitive.long
                val channel = pending.remove(id)
                channel?.send(msg)
                return null
            }
        }
    }

    public suspend inline fun <reified TResult, reified TArgs> call(method: String, params: TArgs): TResult {
        val r = callLowLevel(method, Json.encodeToJsonElement(params))
        if (TResult::class == Unit::class) {
            return Unit as TResult
        }
        return jsonIgnoringUnknownKeys.decodeFromJsonElement(r)
    }

    public suspend fun callLowLevel(method: String, params: JsonElement): JsonElement =
        withContext(this.coroutineContext) {
            val channel = Channel<RpcResponse>(1)
            val id = pending.new(channel)
            val response: RpcResponse
            try {
                adapter.writeMessage(
                    codec.encodeMessage(
                        RpcCallRequest(
                            "2.0",
                            method,
                            params,
                            JsonPrimitive(id)
                        )
                    )
                )
                withTimeout(5000) {
                    response = channel.receive()
                }
            } catch (e: TimeoutCancellationException) {
                pending.remove(id)
                throw e
            } finally {
                channel.close()
            }
            return@withContext when (response) {
                is RpcErrorResponse -> throw RpcTargetException(response.error)
                is RpcResultResponse -> response.result
            }
        }

    public suspend inline fun <reified TArgs> notify(method: String, params: TArgs) {
        notifyLowLevel(method, Json.encodeToJsonElement(params))
    }

    public suspend fun notifyLowLevel(method: String, params: JsonElement): Unit = withContext(this.coroutineContext) {
        adapter.writeMessage(codec.encodeMessage(RpcNotifyRequest("2.0", method, params)))
    }

    public suspend fun join() {
        this.supervisor.join()
    }

    public suspend fun cancelAndJoin() {
        this.supervisor.cancelAndJoin()
    }

    public companion object {
        @JvmStatic
        public val jsonIgnoringUnknownKeys: Json = Json {
            ignoreUnknownKeys = true
        }

        @JvmStatic
        public inline fun <reified T> readParam(params: JsonElement, index: Int, name: String): T? {
            val x = when {
                params is JsonArray && index < params.count() -> {
                    params[index]
                }
                params is JsonObject && params.containsKey(name) -> {
                    params[name]
                }
                else -> {
                    null
                }
            }
            if (x == null) {
                return x
            }
            return Json.decodeFromJsonElement<T>(x)
        }
    }
}