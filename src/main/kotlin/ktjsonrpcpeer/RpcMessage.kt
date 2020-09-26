package ktjsonrpcpeer

import kotlinx.serialization.SerialName

internal abstract class RpcMessage {
    @SerialName("json-rpc")
    abstract val version: String?
}