package ktjsonrpcpeer

class RpcTargetException(val info: RpcError) : Exception(info.message)