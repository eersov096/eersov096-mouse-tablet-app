package com.zjx.ztezscreenshot

import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import kotlin.math.roundToInt

/**
 * Fastest practical backend in this project:
 * a persistent shell session, preferably created through Shizuku, so events avoid
 * coroutine/intent churn and process spawn overhead.
 *
 * This is still not a true kernel/uinput driver path, but it is markedly closer to the metal
 * than accessibility gesture dispatch.
 */
class ShellInputBackend(
    private val context: Context,
) : InputBackend {
    private val shell = PersistentShell(context)
    private var screenWidth = 1920
    private var screenHeight = 1200
    private var pointerX = 300f
    private var pointerY = 300f
    private var pointerDown = false

    init {
        val dm: DisplayMetrics = context.resources.displayMetrics
        screenWidth = dm.widthPixels
        screenHeight = dm.heightPixels
        pointerX = (screenWidth / 2f)
        pointerY = (screenHeight / 2f)
        shell.start()
    }

    override fun inject(frame: InputFrame) {
        when (frame) {
            is MouseMoveFrame -> injectMove(frame)
            is MouseButtonFrame -> injectButton(frame)
            is KeyFrame -> injectKey(frame)
        }
    }

    private fun injectMove(frame: MouseMoveFrame) {
        pointerX = (pointerX + frame.dx).coerceIn(0f, screenWidth.toFloat())
        pointerY = (pointerY + frame.dy).coerceIn(0f, screenHeight.toFloat())
        val action = if (pointerDown) "MOVE" else "MOVE"
        shell.send("input motionevent $action ${pointerX.roundToInt()} ${pointerY.roundToInt()}")
    }

    private fun injectButton(frame: MouseButtonFrame) {
        if (frame.button != 1) {
            Log.d("GGLL", "Only primary mouse button is mapped in shell backend: $frame")
            return
        }

        pointerDown = frame.pressed
        val x = pointerX.roundToInt()
        val y = pointerY.roundToInt()
        val action = if (frame.pressed) "DOWN" else "UP"
        shell.send("input motionevent $action $x $y")
    }

    private fun injectKey(frame: KeyFrame) {
        if (!frame.pressed) return
        shell.send("input keyevent ${frame.keyCode}")
    }

    override fun name(): String = if (shell.isShizukuBacked()) "shizuku-shell" else "shell"

    fun close() {
        shell.close()
    }
}
