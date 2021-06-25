package com.github.arcticlampyrid.ktjsonrpcpeer.internal

import kotlinx.atomicfu.atomic
import kotlin.native.concurrent.TransferMode
import kotlin.native.concurrent.Worker
import kotlin.native.concurrent.WorkerBoundReference
import kotlin.native.concurrent.freeze

internal actual class PendingMap<V> {
    companion object {
        private val worker = Worker.start(false, "KtJsonRpcPeer-PendingMap-StateManager")
    }

    private val state = worker.execute(TransferMode.SAFE, {}) {
        WorkerBoundReference(HashMap<Long, V>()).freeze()
    }.result
    var seq = atomic(0L)

    actual fun allocId(): Long {
        return seq.incrementAndGet()
    }

    @Throws(IllegalStateException::class)
    actual fun set(id: Long, value: V) {
        worker.execute(TransferMode.SAFE, { Triple(state, id.freeze(), value.freeze()) }) {
            it.first.value.run {
                this[it.second] = it.third
            }
        }.result
    }

    actual fun remove(id: Long): V? {
        return worker.execute(TransferMode.SAFE, { Pair(state, id.freeze()) }) {
            it.first.value.run {
                this.remove(it.second).freeze()
            }
        }.result
    }

    actual fun forEach(action: (Map.Entry<Long, V>) -> Unit) {
        worker.execute(TransferMode.SAFE, { Pair(state, action.freeze()) }) {
            it.first.value.run {
                this.forEach(it.second)
            }
        }.result
    }
}