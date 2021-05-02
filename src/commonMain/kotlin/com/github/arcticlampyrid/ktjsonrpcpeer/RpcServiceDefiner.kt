package com.github.arcticlampyrid.ktjsonrpcpeer

public fun interface RpcServiceDefiner {
    public fun RpcServiceDsl.define()
}

public fun RpcServiceDefiner.build(): RpcService = RpcServiceDsl().apply {
    define()
}.run {
    RpcService(method)
}
