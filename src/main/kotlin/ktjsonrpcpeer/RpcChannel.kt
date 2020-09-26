package ktjsonrpcpeer

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

class RpcChannel(private val adapter: RpcMessageAdapter, private val codec: RpcCodec = RpcJsonCodec) {
    private val pending = ConcurrentHashMap<Long, SendChannel<RpcResponse>>()
    private val seq = AtomicLong()
    private val registeredMethod = HashMap<String, suspend (params: JsonElement) -> JsonElement>()
    val completion: Deferred<Unit>

    init {
        completion = GlobalScope.async(Dispatchers.IO) {
            while (true) {
                val msg: ByteArray
                try {
                    msg = adapter.readMessage()
                } catch (e: Exception) {
                    break
                }
                GlobalScope.launch(Dispatchers.IO) {
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
            adapter.writeMessage(codec.encodeMessage(Json.encodeToJsonElement(RpcErrorResponse(JsonNull, RpcError.ParseError))))
            return
        }
        if (root is JsonArray) {
            val responses = ArrayList<RpcResponse>()
            for (x in root) {
                responses.add(handleMsg(x) ?: continue)
            }
            if (responses.size != 0) {
                adapter.writeMessage(codec.encodeMessage(Json.encodeToJsonElement(ListSerializer(RpcMessageSerializer), responses)))
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
            else -> {
                return null
            }
        }
    }

    inline fun <reified TResult, reified TArgs> register(method: String, noinline processor: suspend (params: TArgs) -> TResult) {
        registerLowLevel(method) {
            Json.encodeToJsonElement(processor(Json.decodeFromJsonElement(it)))
        }
    }

    fun registerLowLevel(method: String, processor: suspend (params: JsonElement) -> JsonElement) {
        registeredMethod[method] = processor
    }

    suspend inline fun <reified TResult, reified TArgs> call(method: String, params: TArgs): TResult {
        return Json.decodeFromJsonElement(callLowLevel(method, Json.encodeToJsonElement(params)))
    }

    suspend fun callLowLevel(method: String, params: JsonElement): JsonElement {
        if (completion.isCompleted) {
            throw RpcNotServingException()
        }
        val id = seq.getAndIncrement()
        val channel = Channel<RpcResponse>(1)
        pending[id] = channel
        val response: RpcResponse
        try {
            adapter.writeMessage(codec.encodeMessage(Json.encodeToJsonElement(RpcCallRequest("2.0", method, params, JsonPrimitive(id)))))
            withTimeout(5000) {
                response = channel.receive()
            }
        } catch (e: TimeoutCancellationException) {
            pending.remove(id)
            throw e
        } finally {
            channel.close()
        }
        return when (response) {
            is RpcErrorResponse -> throw RpcTargetException(response.error)
            is RpcResultResponse -> response.result
            else -> throw RpcTargetException(RpcError.InternalError)
        }
    }

    suspend inline fun <reified TArgs> notify(method: String, params: TArgs) {
        notifyLowLevel(method, Json.encodeToJsonElement(params))
    }

    suspend fun notifyLowLevel(method: String, params: JsonElement) {
        if (completion.isCompleted) {
            throw RpcNotServingException()
        }
        adapter.writeMessage(codec.encodeMessage(Json.encodeToJsonElement(RpcNotifyRequest("2.0", method, params))))
    }

    companion object {
        @JvmStatic
        inline fun <reified T> readParam(params: JsonElement, index: Int, name: String): T? {
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