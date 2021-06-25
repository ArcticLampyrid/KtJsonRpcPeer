package com.github.arcticlampyrid.ktjsonrpcpeer.internal


internal expect class PendingMap<V>() {
    fun allocId(): Long
    fun set(id: Long, value: V)
    fun remove(id: Long): V?
    fun forEach(action: (Map.Entry<Long, V>) -> Unit)
}