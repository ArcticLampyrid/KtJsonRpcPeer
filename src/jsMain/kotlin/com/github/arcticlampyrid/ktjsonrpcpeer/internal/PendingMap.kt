package com.github.arcticlampyrid.ktjsonrpcpeer.internal

internal actual class PendingMap<V> {
    private val map = mutableMapOf<Long, V>()
    private var seq = 0L
    actual fun new(value: V): Long = (++seq).also { id ->
        map[id] = value
    }

    actual fun remove(id: Long): V? = map.remove(id)
}