package com.github.arcticlampyrid.ktjsonrpcpeer

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.launch
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

public class RpcOkWebsocketOnceAdapter private constructor(
    private val incoming: Channel<ByteArray>,
    private val outcoming: Channel<ByteArray>
) : RpcMessageAdapter by RpcChannelAdapter(incoming, outcoming), WebSocketListener() {

    public constructor() : this(Channel<ByteArray>(), Channel<ByteArray>())

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        incoming.close()
        outcoming.close()
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        incoming.close(t)
        outcoming.close(t)
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        incoming.trySendBlocking(text.toByteArray()).getOrThrow()
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        incoming.trySendBlocking(bytes.toByteArray()).getOrThrow()
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onOpen(webSocket: WebSocket, response: Response) {
        GlobalScope.launch(Dispatchers.IO) {
            while (true) {
                webSocket.send(ByteString.of(*outcoming.receiveCatching().getOrNull() ?: break))
            }
        }
    }
}