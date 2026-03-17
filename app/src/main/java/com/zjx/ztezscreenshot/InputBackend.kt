package com.zjx.ztezscreenshot

interface InputBackend {
    fun inject(frame: InputFrame)
    fun name(): String
}

class NoopBackend : InputBackend {
    override fun inject(frame: InputFrame) = Unit
    override fun name(): String = "noop"
}
