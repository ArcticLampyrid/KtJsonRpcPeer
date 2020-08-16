package ktjsonrpcpeer

interface RpcMessageAdapter {
    suspend fun readMessage(): ByteArray
    suspend fun writeMessage(msg: ByteArray)
}