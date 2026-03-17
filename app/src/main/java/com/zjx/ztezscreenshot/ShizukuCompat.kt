package com.zjx.ztezscreenshot

import android.content.Context
import android.content.pm.PackageManager

object ShizukuCompat {
    private const val SHIZUKU_CLASS = "rikka.shizuku.Shizuku"

    fun isAvailable(): Boolean = runCatching { Class.forName(SHIZUKU_CLASS) }.isSuccess

    fun checkSelfPermission(context: Context): Int? = runCatching {
        val clazz = Class.forName(SHIZUKU_CLASS)
        clazz.getMethod("checkSelfPermission").invoke(null) as Int
    }.getOrNull()

    fun isPermissionGranted(context: Context): Boolean {
        val value = checkSelfPermission(context) ?: return false
        return value == PackageManager.PERMISSION_GRANTED
    }

    fun requestPermission(requestCode: Int) {
        runCatching {
            val clazz = Class.forName(SHIZUKU_CLASS)
            clazz.getMethod("requestPermission", Int::class.javaPrimitiveType).invoke(null, requestCode)
        }
    }

    fun newShellProcess(): Process? = runCatching {
        val clazz = Class.forName(SHIZUKU_CLASS)
        val method = clazz.getMethod(
            "newProcess",
            Array<String>::class.java,
            Array<String>::class.java,
            String::class.java,
        )
        method.invoke(null, arrayOf("sh"), null, null) as Process
    }.getOrNull()
}
