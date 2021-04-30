package com.github.arcticlampyrid.ktjsonrpcpeer.internal

import kotlin.native.concurrent.*

internal actual class PendingMap<V> {
    internal class PendingMapState<V> {
        val map = mutableMapOf<Long, V>()
        var seq = 0L
    }

    companion object {
        private val worker = Worker.start(false, "KtJsonRpcPeer-PendingMap-StateManager")
    }

    private val state = worker.execute(TransferMode.SAFE, {}) {
        WorkerBoundReference(PendingMapState<V>()).freeze()
    }.result

    @Throws(IllegalStateException::class)
    actual fun new(value: V): Long {
        return worker.execute(TransferMode.SAFE, { Pair(state, value.freeze()) }) {
            it.first.value.run {
                (++seq).also { id ->
                    map[id] = it.second
                }.freeze()
            }
        }.result
    }

    actual fun remove(id: Long): V? {
        return worker.execute(TransferMode.SAFE, { Pair(state, id.freeze()) }) {
            it.first.value.run {
                map.remove(it.second).freeze()
            }
        }.result
    }
}