package com.github.arcticlampyrid.ktjsonrpcpeer.internal

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

internal actual class PendingMap<V> {
    private val map = ConcurrentHashMap<Long, V>()
    private val seq = AtomicLong(0)
    actual fun new(value: V): Long = seq.incrementAndGet().also { id ->
        map[id] = value
    }

    actual fun remove(id: Long): V? = map.remove(id)
}