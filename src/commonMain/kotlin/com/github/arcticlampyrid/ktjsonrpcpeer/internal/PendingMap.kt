package com.github.arcticlampyrid.ktjsonrpcpeer.internal


internal expect class PendingMap<V>() {
    fun new(value : V) : Long
    fun remove(id: Long) : V?
}