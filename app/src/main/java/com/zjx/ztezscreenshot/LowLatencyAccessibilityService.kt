package com.zjx.ztezscreenshot

import android.accessibilityservice.AccessibilityService
import android.util.DisplayMetrics
import android.view.accessibility.AccessibilityEvent

class LowLatencyAccessibilityService : AccessibilityService() {
    companion object {
        @Volatile var instance: LowLatencyAccessibilityService? = null
    }

    var pointerX: Float = 300f
    var pointerY: Float = 300f
    var screenWidth: Int = 1920
    var screenHeight: Int = 1200

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        val dm: DisplayMetrics = resources.displayMetrics
        screenWidth = dm.widthPixels
        screenHeight = dm.heightPixels
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit
    override fun onInterrupt() = Unit

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }
}
