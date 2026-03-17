package com.zjx.ztezscreenshot

import android.content.Context

class AppConfig(context: Context) {
    private val prefs = context.getSharedPreferences("ggll_config", Context.MODE_PRIVATE)

    fun backendMode(): BackendMode = runCatching {
        BackendMode.valueOf(prefs.getString(KEY_BACKEND, BackendMode.SHIZUKU_SHELL.name)!!)
    }.getOrElse { BackendMode.SHIZUKU_SHELL }

    fun setBackendMode(mode: BackendMode) {
        prefs.edit().putString(KEY_BACKEND, mode.name).apply()
    }

    fun mousePollHz(): Int = prefs.getInt(KEY_MOUSE_POLL_HZ, 1000).coerceIn(125, 1000)

    fun setMousePollHz(value: Int) {
        prefs.edit().putInt(KEY_MOUSE_POLL_HZ, value.coerceIn(125, 1000)).apply()
    }

    companion object {
        private const val KEY_BACKEND = "backend_mode"
        private const val KEY_MOUSE_POLL_HZ = "mouse_poll_hz"
    }
}
