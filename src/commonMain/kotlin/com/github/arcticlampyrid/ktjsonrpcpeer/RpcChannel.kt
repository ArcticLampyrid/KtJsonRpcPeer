@file:OptIn(RpcInternalModelApi::class)

package com.github.arcticlampyrid.ktjsonrpcpeer

import com.github.arcticlampyrid.ktjsonrpcpeer.internal.PendingMap
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.*
import kotlinx.serialization.serializer
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.resume
import kotlin.jvm.JvmName
import kotlin.jvm.JvmStatic

public class RpcChannel(
    private val adapter: RpcMessageAdapter,
    private val codec: RpcCodec = RpcJsonCodec,
    parentCoroutineContext: CoroutineContext = EmptyCoroutineContext,
    service: RpcService = RpcService.Empty
) : CoroutineScope {
    private val supervisor = SupervisorJob(parentCoroutineContext[Job])
    override val coroutineContext: CoroutineContext = parentCoroutineContext + supervisor

    private val pending = PendingMap<CancellableContinuation<RpcResponse>>()
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
        supervisor.invokeOnCompletion {
            pending.forEach {
                it.value.cancel(CancellationException("RpcChannel is closing"))
            }
        }
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
        return when (msg) {
            is RpcRequest -> _service.value.handleRequest(msg)
            is RpcResponse -> {
                val id = msg.id.jsonPrimitive.long
                pending.remove(id)?.resume(msg)
                null
            }
        }
    }

    @JvmName("callReified")
    public suspend inline fun <reified TResult, reified TArgs> call(method: String, params: TArgs): TResult {
        val resultDeserializer = serializer<TResult>()
        val argSerializer = serializer<TArgs>()
        return call(method, resultDeserializer, argSerializer, params)
    }

    @Suppress("NOTHING_TO_INLINE")
    public suspend inline fun <TResult, TArgs> call(
        method: String,
        resultDeserializer: DeserializationStrategy<TResult>,
        argSerializer: SerializationStrategy<TArgs>,
        params: TArgs
    ): TResult {
        val r = callLowLevel(
            method, when (argSerializer) {
                Unit.serializer() -> JsonNull
                else -> Json.encodeToJsonElement(argSerializer, params)
            }
        )
        @Suppress("UNCHECKED_CAST")
        return when (resultDeserializer) {
            Unit.serializer() -> Unit as TResult
            else -> jsonIgnoringUnknownKeys.decodeFromJsonElement(resultDeserializer, r)
        }
    }

    public suspend fun callLowLevel(method: String, params: JsonElement): JsonElement {
        val id = pending.allocId()
        val response: RpcResponse
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
            response = suspendCancellableCoroutine { cont ->
                pending.set(id, cont)
                cont.invokeOnCancellation {
                    pending.remove(id)
                }
                if (!isActive && cont.isActive) {
                    cont.cancel(CancellationException("RpcChannel is closed"))
                }
            }
        }
        return when (response) {
            is RpcErrorResponse -> throw RpcTargetException(response.error)
            is RpcResultResponse -> response.result
        }
    }

    @JvmName("notifyReified")
    public suspend inline fun <reified TArgs> notify(method: String, params: TArgs) {
        val argSerializer = serializer<TArgs>()
        notify(method, argSerializer, params)
    }

    @Suppress("NOTHING_TO_INLINE")
    public suspend inline fun <TArgs> notify(
        method: String,
        argSerializer: SerializationStrategy<TArgs>,
        params: TArgs
    ) {
        notifyLowLevel(
            method, when (argSerializer) {
                Unit.serializer() -> JsonNull
                else -> Json.encodeToJsonElement(argSerializer, params)
            }
        )
    }

    public suspend fun notifyLowLevel(method: String, params: JsonElement) {
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