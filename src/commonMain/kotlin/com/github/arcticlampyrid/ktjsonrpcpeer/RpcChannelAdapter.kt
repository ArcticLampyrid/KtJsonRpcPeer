package com.github.arcticlampyrid.ktjsonrpcpeer

import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel

public class RpcChannelAdapter(
    private val incoming: ReceiveChannel<ByteArray>,
    private val outcoming: SendChannel<ByteArray>
) : RpcMessageAdapter {
    override suspend fun readMessage(): ByteArray {
        return incoming.receive()
    }

    override suspend fun writeMessage(msg: ByteArray) {
        outcoming.send(msg)
    }
}