package com.github.arcticlampyrid.ktjsonrpcpeer

public class RpcTargetException(public val info: RpcError) : Exception(info.message)