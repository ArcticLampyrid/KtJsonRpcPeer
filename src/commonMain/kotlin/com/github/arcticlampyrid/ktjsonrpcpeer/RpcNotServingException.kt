package com.github.arcticlampyrid.ktjsonrpcpeer

public class RpcNotServingException(cause: Throwable? = null) : Exception("rpc not serving exception", cause)