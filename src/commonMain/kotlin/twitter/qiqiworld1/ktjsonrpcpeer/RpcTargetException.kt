package twitter.qiqiworld1.ktjsonrpcpeer

public class RpcTargetException(public val info: RpcError) : Exception(info.message)