package com.github.arcticlampyrid.ktjsonrpcpeer.ktjsonrpcpeer

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
        val id = x.new("Hello")
        assertEquals("Hello", x.remove(id))
    }

    @Test
    fun multiThreadTest() {
        val x = PendingMap<String>().freeze()
        val id = x.new("Hello")
        withWorker {
            execute(TransferMode.SAFE, { Pair(x, id) }) {
                it.first.remove(it.second)
            }.consume {
                assertEquals("Hello", it)
            }
        }
    }
}