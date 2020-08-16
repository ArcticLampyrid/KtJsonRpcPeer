package ktjsonrpcpeer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.launch
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.nio.charset.Charset

class RpcOkWebsocketOnceAdapter private constructor(private val incoming : Channel<ByteArray>,
                                                    private val outcoming : Channel<ByteArray>)
    : RpcMessageAdapter by RpcChannelAdapter(incoming, outcoming), WebSocketListener() {

    constructor():this(Channel<ByteArray>(), Channel<ByteArray>()){

    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        incoming.close()
        outcoming.close()
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        incoming.close()
        outcoming.close()
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        incoming.sendBlocking(text.toByteArray())
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        incoming.sendBlocking(bytes.toByteArray())
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        GlobalScope.launch(Dispatchers.IO) {
            while (true) {
                webSocket.send(ByteString.of(*outcoming.receiveOrNull() ?: break))
            }
        }
    }
}