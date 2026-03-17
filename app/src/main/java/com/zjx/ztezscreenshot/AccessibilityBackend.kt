package com.zjx.ztezscreenshot

import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.util.Log

/**
 * Accessibility gesture injection still has platform-imposed limits,
 * but this backend removes coroutine hopping and posts directly to the main thread.
 */
class AccessibilityBackend(
    private val serviceProvider: () -> LowLatencyAccessibilityService?,
) : InputBackend {

    private val mainHandler = Handler(Looper.getMainLooper())

    override fun inject(frame: InputFrame) {
        when (frame) {
            is MouseMoveFrame -> injectMove(frame)
            is MouseButtonFrame -> Log.d("GGLL", "Button frame: $frame")
            is KeyFrame -> Log.d("GGLL", "Key frame: $frame")
        }
    }

    private fun injectMove(frame: MouseMoveFrame) {
        val service = serviceProvider() ?: return
        val newX = (service.pointerX + frame.dx).coerceIn(0f, service.screenWidth.toFloat())
        val newY = (service.pointerY + frame.dy).coerceIn(0f, service.screenHeight.toFloat())
        service.pointerX = newX
        service.pointerY = newY

        mainHandler.post {
            val currentService = serviceProvider() ?: return@post
            val path = Path().apply {
                moveTo(currentService.pointerX, currentService.pointerY)
                lineTo(currentService.pointerX + 0.1f, currentService.pointerY + 0.1f)
            }
            val stroke = GestureDescription.StrokeDescription(path, 0, 1, true)
            val gesture = GestureDescription.Builder().addStroke(stroke).build()
            currentService.dispatchGesture(gesture, null, null)
        }
    }

    override fun name(): String = "accessibility"
}
