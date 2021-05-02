package com.github.arcticlampyrid.ktjsonrpcpeer

import com.github.arcticlampyrid.ktjsonrpcpeer.internal.PendingMap
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.jvm.JvmStatic

public class RpcChannel(
    private val adapter: RpcMessageAdapter,
    private val codec: RpcCodec = RpcJsonCodec,
    private val parentCoroutineContext: CoroutineContext = EmptyCoroutineContext
) : CoroutineScope {
    private val supervisor = SupervisorJob(parentCoroutineContext[Job])
    private val _coroutineContext = parentCoroutineContext + supervisor
    override val coroutineContext: CoroutineContext
        get() = _coroutineContext

    private val pending = PendingMap<SendChannel<RpcResponse>>()
    private val registeredMethod = HashMap<String, suspend (params: JsonElement) -> JsonElement>()

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
        val root: JsonElement
        try {
            root = codec.decodeMessage(msg)
        } catch (e: Exception) {
            adapter.writeMessage(
                codec.encodeMessage(
                    Json.encodeToJsonElement(
                        RpcErrorResponse(
                            JsonNull,
                            RpcError.ParseError
                        )
                    )
                )
            )
            return
        }
        if (root is JsonArray) {
            val responses = ArrayList<RpcResponse>()
            for (x in root) {
                responses.add(handleMsg(x) ?: continue)
            }
            if (responses.size != 0) {
                adapter.writeMessage(
                    codec.encodeMessage(
                        Json.encodeToJsonElement(
                            ListSerializer(RpcMessageSerializer),
                            responses
                        )
                    )
                )
            }
        } else {
            val r = handleMsg(root)
            if (r != null) {
                adapter.writeMessage(codec.encodeMessage(Json.encodeToJsonElement(RpcMessageSerializer, r)))
            }
        }
    }

    private suspend fun handleMsg(msg: JsonElement): RpcResponse? {
        if (msg !is JsonObject) {
            return RpcErrorResponse(JsonNull, RpcError.InvalidRequest)
        }
        return handleMsg(Json.decodeFromJsonElement(RpcMessageSerializer, msg))
    }

    private suspend fun handleMsg(msg: RpcMessage): RpcResponse? {
        when (msg) {
            is RpcNotifyRequest -> {
                val processor = registeredMethod[msg.method] ?: return null
                processor(msg.params)
                return null
            }
            is RpcCallRequest -> {
                val processor = registeredMethod[msg.method]
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

    public inline fun <reified TResult, reified TArgs> register(
        method: String,
        noinline processor: suspend (params: TArgs) -> TResult
    ) {
        registerLowLevel(method) {
            val p = if (TArgs::class == Unit::class) {
                Unit as TArgs
            } else {
                Json.decodeFromJsonElement<TArgs>(it)
            }
            val r = processor(p)
            if (TResult::class == Unit::class) {
                JsonNull
            } else {
                Json.encodeToJsonElement(r)
            }
        }
    }

    public fun registerLowLevel(method: String, processor: suspend (params: JsonElement) -> JsonElement) {
        registeredMethod[method] = processor
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
                        Json.encodeToJsonElement(
                            RpcCallRequest(
                                "2.0",
                                method,
                                params,
                                JsonPrimitive(id)
                            )
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
        adapter.writeMessage(codec.encodeMessage(Json.encodeToJsonElement(RpcNotifyRequest("2.0", method, params))))
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