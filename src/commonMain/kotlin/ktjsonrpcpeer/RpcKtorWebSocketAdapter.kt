package ktjsonrpcpeer

import io.ktor.http.cio.websocket.*

public class RpcKtorWebSocketAdapter(private val session: WebSocketSession) : RpcMessageAdapter {
    override suspend fun readMessage(): ByteArray =
        when (val frame = session.incoming.receive()) {
            is Frame.Text -> frame.readText().encodeToByteArray()
            is Frame.Binary -> frame.readBytes()
            else -> throw Exception("not support to process " + frame.frameType.name)
        }

    override suspend fun writeMessage(msg: ByteArray) {
        session.send(Frame.Binary(true, msg))
    }
}