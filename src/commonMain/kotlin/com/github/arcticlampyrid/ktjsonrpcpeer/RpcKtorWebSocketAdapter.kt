package com.github.arcticlampyrid.ktjsonrpcpeer

import io.ktor.http.cio.websocket.*

public class RpcKtorWebSocketAdapter(private val session: WebSocketSession) : RpcMessageAdapter {
    override suspend fun readMessage(): ByteArray =
        when (val frame = session.incoming.receive()) {
            is Frame.Text, is Frame.Binary -> frame.run {
                require(fin)
                data
            }
            else -> throw Exception("not support to process " + frame.frameType.name)
        }

    override suspend fun writeMessage(msg: ByteArray) {
        session.send(Frame.Binary(true, msg))
    }
}