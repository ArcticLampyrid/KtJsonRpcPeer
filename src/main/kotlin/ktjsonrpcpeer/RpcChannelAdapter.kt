package ktjsonrpcpeer

import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.withTimeout

public class RpcChannelAdapter(private val incoming: ReceiveChannel<ByteArray>, private val outcoming: SendChannel<ByteArray>)
    : RpcMessageAdapter {
    override suspend fun readMessage(): ByteArray {
        return incoming.receive()
    }

    override suspend fun writeMessage(msg: ByteArray) {
        withTimeout(5000) {
            outcoming.send(msg)
        }
    }
}