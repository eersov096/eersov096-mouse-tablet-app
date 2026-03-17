package com.zjx.ztezscreenshot

sealed interface InputFrame {
    val eventTimeNanos: Long
}

data class MouseMoveFrame(
    val dx: Float,
    val dy: Float,
    override val eventTimeNanos: Long,
) : InputFrame

data class MouseButtonFrame(
    val button: Int,
    val pressed: Boolean,
    override val eventTimeNanos: Long,
) : InputFrame

data class KeyFrame(
    val keyCode: Int,
    val pressed: Boolean,
    override val eventTimeNanos: Long,
) : InputFrame
