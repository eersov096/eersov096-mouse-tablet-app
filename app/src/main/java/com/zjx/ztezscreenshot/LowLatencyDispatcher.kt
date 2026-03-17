package com.zjx.ztezscreenshot

import android.os.Process
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.LockSupport
import kotlin.math.max

/**
 * Dedicated urgent threads avoid coroutine scheduler jitter.
 *
 * - mouseThread: fixed-rate high-priority pump with sub-ms sleep strategy
 * - discreteThread: immediate dispatch for buttons/keys with tiny backoff when idle
 */
class LowLatencyDispatcher(
    private val backend: InputBackend,
    private val queue: LowLatencyInputQueue,
    private val pollHz: Int = 500,
) {
    private val running = AtomicBoolean(false)
    @Volatile private var mouseThread: Thread? = null
    @Volatile private var discreteThread: Thread? = null

    fun start() {
        if (!running.compareAndSet(false, true)) return

        discreteThread = Thread({
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_DISPLAY)
            while (running.get()) {
                val first = queue.pollDiscrete()
                if (first != null) {
                    backend.inject(first)
                    val batch = queue.drainDiscrete(maxItems = 15)
                    for (item in batch) backend.inject(item)
                } else {
                    LockSupport.parkNanos(250_000L)
                }
            }
        }, "ggll-discrete").apply { start() }

        mouseThread = Thread({
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_DISPLAY)
            val tickNanos = max(250_000L, 1_000_000_000L / pollHz.toLong())
            var nextTick = System.nanoTime()

            while (running.get()) {
                queue.drainLatestMouse()?.let(backend::inject)
                nextTick += tickNanos
                sleepUntil(nextTick)

                val now = System.nanoTime()
                if (now - nextTick > tickNanos * 2) {
                    nextTick = now
                }
            }
        }, "ggll-mouse").apply { start() }
    }

    fun stop() {
        if (!running.compareAndSet(true, false)) return
        mouseThread?.interrupt()
        discreteThread?.interrupt()
        mouseThread = null
        discreteThread = null
    }

    fun shutdown() = stop()

    private fun sleepUntil(targetNanos: Long) {
        while (running.get()) {
            val remaining = targetNanos - System.nanoTime()
            when {
                remaining <= 0L -> return
                remaining > 2_000_000L -> LockSupport.parkNanos(remaining - 1_000_000L)
                remaining > 200_000L -> LockSupport.parkNanos(remaining - 100_000L)
                else -> Thread.onSpinWait()
            }
        }
    }
}
