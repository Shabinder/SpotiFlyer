/*
 *  * Copyright (c)  2021  Shabinder Singh
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  *  You should have received a copy of the GNU General Public License
 *  *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.shabinder.common.di.utils

// Dependencies:
// implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9-native-mt")
// implementation("org.jetbrains.kotlinx:atomicfu:0.14.4")
// Gist: https://gist.github.com/fluidsonic/ba32de21c156bbe8424c8d5fc20dcd8e

import com.shabinder.common.di.dispatcherIO
import io.ktor.utils.io.core.Closeable
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class ParallelExecutor(
    parentContext: CoroutineContext = dispatcherIO,
) : Closeable {

    private val concurrentOperationLimit = atomic(4)
    private val coroutineContext = parentContext + Job()
    private var isClosed = atomic(false)
    private val killQueue = Channel<Unit>(Channel.UNLIMITED)
    private val operationQueue = Channel<Operation<*>>(Channel.RENDEZVOUS)

    init {
        startOrStopProcessors(expectedCount = concurrentOperationLimit.value, actualCount = 0)
    }

    override fun close() {
        if (!isClosed.compareAndSet(expect = false, update = true))
            return

        val cause = CancellationException("Executor was closed.")

        killQueue.close(cause)
        operationQueue.close(cause)
        coroutineContext.cancel(cause)
    }

    private fun CoroutineScope.launchProcessor() = launch {
        while (true) {
            val operation = select<Operation<*>?> {
                killQueue.onReceive { null }
                operationQueue.onReceive { it }
            } ?: break

            operation.execute()
        }
    }

    suspend fun <Result> execute(block: suspend () -> Result): Result =
        withContext(coroutineContext) {
            val operation = Operation(block)
            operationQueue.send(operation)

            operation.result.await()
        }

    // TODO This launches all coroutines in advance even if they're never needed. Find a lazy way to do this.
    fun setConcurrentOperationLimit(limit: Int) {
        require(limit >= 1) { "'limit' must be greater than zero: $limit" }
        require(limit < 1_000_000) { "Don't use a very high limit because it will cause a lot of coroutines to be started eagerly: $limit" }

        startOrStopProcessors(expectedCount = limit, actualCount = concurrentOperationLimit.getAndSet(limit))
    }

    private fun startOrStopProcessors(expectedCount: Int, actualCount: Int) {
        if (expectedCount == actualCount)
            return

        if (isClosed.value)
            return

        var change = expectedCount - actualCount
        while (change > 0 && killQueue.poll() != null)
            change -= 1

        if (change > 0)
            with(CoroutineScope(coroutineContext)) {
                repeat(change) { launchProcessor() }
            }
        else
            repeat(-change) { killQueue.offer(Unit) }
    }

    private class Operation<Result>(
        private val block: suspend () -> Result,
    ) {

        private val _result = CompletableDeferred<Result>()

        val result: Deferred<Result> get() = _result

        suspend fun execute() {
            try {
                _result.complete(block())
            } catch (e: Throwable) {
                _result.completeExceptionally(e)
            }
        }
    }
}

/*
suspend fun main() = coroutineScope {
    val executor = ParallelExecutor(coroutineContext)

    println("Concurrency: 1")

    coroutineScope {
        (1 .. 200).forEach { i ->
            launch {
                executor.execute {
                    println("Execution $i")
                    delay(250)

                    when (i) {
                        10 -> {
                            println("Concurrency: 5")
                            executor.setConcurrentOperationLimit(5)
                        }

                        100 -> {
                            println("Concurrency: 1")
                            executor.setConcurrentOperationLimit(1)
                        }

                        110 -> {
                            println("Closing executor")
                            executor.close()
                        }
                    }
                }
            }
            delay(1)
        }
    }

    println("Fin.")
}*/
