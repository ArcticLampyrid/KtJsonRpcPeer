package com.github.arcticlampyrid.ktjsonrpcpeer

import com.github.arcticlampyrid.ktjsonrpcpeer.internal.PendingMap
import kotlin.native.concurrent.TransferMode
import kotlin.native.concurrent.freeze
import kotlin.native.concurrent.withWorker
import kotlin.test.Test
import kotlin.test.assertEquals

class PendingMapTest {
    @Test
    fun basicTest() {
        val x = PendingMap<String>().freeze()
        val id = x.allocId().also { x.set(it, "Hello") }
        x.forEach { entry ->
            assertEquals(id, entry.key)
            assertEquals("Hello", entry.value)
        }
        assertEquals("Hello", x.remove(id))
    }

    @Test
    fun multiThreadTest() {
        val x = PendingMap<String>().freeze()
        val id = x.allocId().also { x.set(it, "Hello") }
        withWorker {
            execute(TransferMode.SAFE, { Pair(x, id) }) {
                it.first.forEach { entry ->
                    assertEquals(it.second, entry.key)
                    assertEquals("Hello", entry.value)
                }
                it.first.remove(it.second)
            }.consume {
                assertEquals("Hello", it)
            }
        }
    }

    @Test
    fun multiThreadTestWithCreatingOnNonMainThread() {
        withWorker {
            execute(TransferMode.SAFE, {}) {
                val x = PendingMap<String>().freeze()
                val id = x.allocId().also { x.set(it, "Hello") }.freeze()
                Pair(x, id).freeze()
            }.consume {
                it.first.forEach { entry ->
                    assertEquals(it.second, entry.key)
                    assertEquals("Hello", entry.value)
                }
                assertEquals("Hello", it.first.remove(it.second))
            }
        }
    }
}