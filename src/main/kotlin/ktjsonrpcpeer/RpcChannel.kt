package ktjsonrpcpeer

import com.google.gson.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

class RpcChannel(private val adapter: RpcMessageAdapter, private val codec: RpcCodec = RpcJsonCodec) {
    private val pending = ConcurrentHashMap<Long, SendChannel<JsonObject>>()
    private val seq = AtomicLong()
    private val registeredMethod = HashMap<String, suspend (params: JsonElement) -> Any>()
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
            root = gson.fromJson(codec.decodeMessage(msg), JsonElement::class.java)
        } catch (e: Exception) {
            adapter.writeMessage(codec.encodeMessage(gson.toJsonTree(RpcErrorResponse(JsonNull.INSTANCE, RpcError.ParseError))))
            return
        }
        val responses = ArrayList<RpcResponse>()
        if (root.isJsonArray) {
            for (x in root.asJsonArray) {
                val r = handleMsg(x)
                if (r != null) {
                    responses.add(r)
                }
            }
        } else {
            val r = handleMsg(root)
            if (r != null) {
                responses.add(r)
            }
        }
        if (responses.size == 0) {
            return
        }
        if (root.isJsonArray) {
            adapter.writeMessage(codec.encodeMessage(gson.toJsonTree(responses)))
        } else {
            adapter.writeMessage(codec.encodeMessage(gson.toJsonTree(responses[0])))
        }
    }

    private suspend fun handleMsg(msg: JsonElement): RpcResponse? {
        if (!msg.isJsonObject) {
            return RpcErrorResponse(JsonNull.INSTANCE, RpcError.InvalidRequest)
        }
        return handleMsg(msg.asJsonObject)
    }

    private suspend fun handleMsg(msg: JsonObject): RpcResponse? {
        if (msg.has("method")) {
            val request: RpcRequest
            try {
                request = gson.fromJson(msg, RpcRequest::class.java)
            } catch (e: Exception) {
                return RpcErrorResponse(JsonNull.INSTANCE, RpcError.InvalidRequest)
            }
            val processor = registeredMethod.get(request.method)
            if (processor == null) {
                if (!msg.has("id")) {
                    return null
                }
                return RpcErrorResponse(msg["id"], RpcError.MothedNotFound)
            }
            try {
                val result = processor(request.params)
                val jsonResult = when (result) {
                    is Unit -> JsonNull.INSTANCE
                    else -> gson.toJsonTree(result)
                }
                if (!msg.has("id")) {
                    return null
                }
                return RpcResultResponse(msg["id"], jsonResult)
            } catch (e: RpcTargetException) {
                if (!msg.has("id")) {
                    return null
                }
                return RpcErrorResponse(msg["id"], e.info)
            } catch (e: Exception) {
                if (!msg.has("id")) {
                    return null
                }
                return RpcErrorResponse(msg["id"], RpcError(-32000, e.message ?: "Unknown error"))
            }
        } else {
            try {
                val id = msg["id"].asLong
                val channel = pending.remove(id)
                channel?.send(msg)
            } catch (e: Exception) {

            }
            return null
        }
    }

    fun register(method: String, funObject: suspend (params: JsonElement) -> Any) {
        registeredMethod[method] = funObject
    }

    suspend inline fun <reified T> call(method: String, params: Any): T {
        return call(method, params, T::class.java)
    }

    suspend fun <T> call(method: String, params: Any, clazz: Class<T>): T {
        val result = call(method, gson.toJsonTree(params))
        if (clazz == Unit::class.java) {
            return Unit as T
        }
        return gson.fromJson(result, clazz)
    }

    private suspend fun call(method: String, params: JsonElement): JsonElement {
        if (completion.isCompleted) {
            throw RpcNotServingException()
        }
        val id = seq.getAndIncrement()
        val channel = Channel<JsonObject>(1)
        pending[id] = channel
        val response: JsonObject
        try {
            adapter.writeMessage(codec.encodeMessage(gson.toJsonTree(RpcCallRequest("2.0", method, params, JsonPrimitive(id)))))
            withTimeout(5000) {
                response = channel.receive()
            }
        } catch (e: TimeoutCancellationException) {
            pending.remove(id)
            throw e
        } finally {
            channel.close()
        }
        if (response.has("error") && !response["error"].isJsonNull) {
            val error = gson.fromJson(response["error"], RpcError::class.java)
            throw RpcTargetException(error)
        }
        if (!response.has("result")){
            throw RpcTargetException(RpcError.InternalError)
        }
        return response["result"]
    }

    suspend fun notify(method: String, params: Any) {
        notify(method, gson.toJsonTree(params))
    }

    suspend fun notify(method: String, params: JsonElement) {
        if (completion.isCompleted) {
            throw RpcNotServingException()
        }
        adapter.writeMessage(codec.encodeMessage(gson.toJsonTree(RpcRequest("2.0", method, params))))
    }

    companion object {
        internal val gson = GsonBuilder().serializeNulls().create()
        @JvmStatic
        inline fun <reified T> readParam(params: JsonElement, index: Int, name: String): T? {
            return readParam<T>(params, index, name, T::class.java)
        }

        @JvmStatic
        fun <T> readParam(params: JsonElement, index: Int, name: String, clazz: Class<T>): T? {
            val x = when {
                params.isJsonArray && index < params.asJsonArray.size() -> {
                    params.asJsonArray[index]
                }
                params.isJsonObject && params.asJsonObject.has(name) -> {
                    params.asJsonObject[name]
                }
                else -> {
                    return null
                }
            }
            return gson.fromJson(x, clazz)
        }
    }
}