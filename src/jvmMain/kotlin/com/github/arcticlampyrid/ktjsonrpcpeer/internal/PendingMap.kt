package com.github.arcticlampyrid.ktjsonrpcpeer.internal

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

internal actual class PendingMap<V> {
    private val map = ConcurrentHashMap<Long, V>()
    private val seq = AtomicLong(0)
    actual fun allocId(): Long = seq.incrementAndGet()
    actual fun set(id: Long, value: V) {
        map[id] = value
    }

    actual fun remove(id: Long): V? = map.remove(id)
    actual fun forEach(action: (Map.Entry<Long, V>) -> Unit) {
        map.forEach(action)
    }
}