package com.zjx.ztezscreenshot

import android.content.Context

object BackendFactory {
    fun create(context: Context, mode: BackendMode): InputBackend = when (mode) {
        BackendMode.SHIZUKU_SHELL -> ShellInputBackend(context)
        BackendMode.ACCESSIBILITY -> AccessibilityBackend { LowLatencyAccessibilityService.instance }
        BackendMode.NOOP -> NoopBackend()
    }

    fun shutdown(backend: InputBackend) {
        if (backend is ShellInputBackend) {
            backend.close()
        }
    }
}
