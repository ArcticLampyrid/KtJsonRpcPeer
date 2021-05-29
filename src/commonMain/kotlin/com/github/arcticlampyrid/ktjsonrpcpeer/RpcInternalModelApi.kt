package com.github.arcticlampyrid.ktjsonrpcpeer

@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "Rpc Internal Model API is less stable than Normal API. " +
            "Use this API carefully. " +
            "It should be only used within codec implementation."
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
public annotation class RpcInternalModelApi