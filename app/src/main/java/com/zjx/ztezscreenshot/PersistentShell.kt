package com.zjx.ztezscreenshot

import android.content.Context
import android.util.Log
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.util.concurrent.atomic.AtomicBoolean

class PersistentShell(private val context: Context) {
    private val started = AtomicBoolean(false)
    @Volatile private var process: Process? = null
    @Volatile private var writer: BufferedWriter? = null
    @Volatile private var shizukuBacked = false

    fun start(): Boolean {
        if (!started.compareAndSet(false, true)) return true

        val created = when {
            ShizukuCompat.isPermissionGranted(context) -> {
                ShizukuCompat.newShellProcess()?.also { shizukuBacked = true }
            }
            else -> null
        } ?: Runtime.getRuntime().exec(arrayOf("sh"))

        process = created
        writer = BufferedWriter(OutputStreamWriter(created.outputStream))
        send("echo ggll-shell-ready >/dev/null")
        return true
    }

    fun send(command: String): Boolean = runCatching {
        if (!started.get()) start()
        val target = writer ?: return false
        target.write(command)
        target.newLine()
        target.flush()
        true
    }.getOrElse {
        Log.e("GGLL", "Shell command failed: $command", it)
        false
    }

    fun isShizukuBacked(): Boolean = shizukuBacked

    fun close() {
        runCatching { writer?.write("exit\n") }
        runCatching { writer?.flush() }
        runCatching { writer?.close() }
        runCatching { process?.destroy() }
        writer = null
        process = null
        started.set(false)
        shizukuBacked = false
    }
}
