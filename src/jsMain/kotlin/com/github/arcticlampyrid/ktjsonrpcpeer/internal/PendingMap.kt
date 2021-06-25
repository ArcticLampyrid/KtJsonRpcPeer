package com.github.arcticlampyrid.ktjsonrpcpeer.internal

internal actual class PendingMap<V> {
    private val map = mutableMapOf<Long, V>()
    private var seq = 0L
    actual fun allocId(): Long = ++seq
    actual fun set(id: Long, value: V) {
        map[id] = value
    }

    actual fun remove(id: Long): V? = map.remove(id)
    actual fun forEach(action: (Map.Entry<Long, V>) -> Unit) {
        map.forEach(action)
    }
}