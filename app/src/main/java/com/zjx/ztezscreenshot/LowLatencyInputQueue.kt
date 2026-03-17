package com.zjx.ztezscreenshot

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicLong

/**
 * Low-overhead input queue:
 * - mouse movement is accumulated to prevent backlog growth
 * - discrete events remain ordered and lossless
 * - a monotonically increasing sequence is kept for debugging and future profiling
 */
class LowLatencyInputQueue {
    private val mouseLock = Any()
    private var mouseDx = 0f
    private var mouseDy = 0f
    private var mouseEventTimeNanos = 0L
    private val mouseSequence = AtomicLong(0)

    private val discrete = ConcurrentLinkedQueue<InputFrame>()

    fun offer(frame: InputFrame) {
        when (frame) {
            is MouseMoveFrame -> synchronized(mouseLock) {
                mouseDx += frame.dx
                mouseDy += frame.dy
                mouseEventTimeNanos = frame.eventTimeNanos
                mouseSequence.incrementAndGet()
            }
            else -> discrete.offer(frame)
        }
    }

    fun drainLatestMouse(): MouseMoveFrame? = synchronized(mouseLock) {
        if (mouseDx == 0f && mouseDy == 0f) {
            null
        } else {
            val frame = MouseMoveFrame(
                dx = mouseDx,
                dy = mouseDy,
                eventTimeNanos = mouseEventTimeNanos,
            )
            mouseDx = 0f
            mouseDy = 0f
            mouseEventTimeNanos = 0L
            frame
        }
    }

    fun pollDiscrete(): InputFrame? = discrete.poll()

    fun drainDiscrete(maxItems: Int): List<InputFrame> {
        if (maxItems <= 0) return emptyList()
        val out = ArrayList<InputFrame>(maxItems)
        repeat(maxItems) {
            val item = discrete.poll() ?: return@repeat
            out += item
        }
        return out
    }
}
