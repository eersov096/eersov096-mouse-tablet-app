package com.zjx.ztezscreenshot

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val config = AppConfig(this)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val pad = (16 * resources.displayMetrics.density).toInt()
            setPadding(pad, pad, pad, pad)
        }

        val title = TextView(this).apply {
            text = "GG Mouse Low Latency v3"
            textSize = 22f
        }
        val desc = TextView(this).apply {
            text = "Лучший практический вариант в этом проекте: persistent shell backend через Shizuku, 1000 Hz mouse pump и отдельный immediate-dispatch для key/button."
        }

        val backendLabel = TextView(this).apply { text = "Backend" }
        val backendSpinner = Spinner(this).apply {
            adapter = ArrayAdapter(
                this@MainActivity,
                android.R.layout.simple_spinner_dropdown_item,
                BackendMode.entries.map { it.name }
            )
            setSelection(BackendMode.entries.indexOf(config.backendMode()).coerceAtLeast(0))
        }

        val hzLabel = TextView(this).apply { text = "Mouse poll Hz" }
        val hzSpinner = Spinner(this).apply {
            val values = listOf("125", "250", "500", "1000")
            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, values)
            val idx = values.indexOf(config.mousePollHz().toString()).takeIf { it >= 0 } ?: values.lastIndex
            setSelection(idx)
        }

        val shizukuState = TextView(this).apply {
            text = when {
                !ShizukuCompat.isAvailable() -> "Shizuku API не найдена"
                ShizukuCompat.isPermissionGranted(this@MainActivity) -> "Shizuku: доступ выдан"
                else -> "Shizuku: нужен grant permission"
            }
        }

        val requestShizuku = Button(this).apply {
            text = "Request Shizuku permission"
            setOnClickListener {
                ShizukuCompat.requestPermission(1001)
                shizukuState.text = "Shizuku: запрос отправлен"
            }
        }

        val save = Button(this).apply {
            text = "Save config"
            setOnClickListener {
                config.setBackendMode(BackendMode.valueOf(backendSpinner.selectedItem as String))
                config.setMousePollHz((hzSpinner.selectedItem as String).toInt())
                shizukuState.text = "Конфиг сохранён. Перезапусти сервис."
            }
        }

        val start = Button(this).apply {
            text = "Start service"
            setOnClickListener {
                if (Build.VERSION.SDK_INT >= 33) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                startForegroundService(Intent(this@MainActivity, ForegroundInputService::class.java))
            }
        }

        layout.addView(title)
        layout.addView(desc)
        layout.addView(backendLabel)
        layout.addView(backendSpinner)
        layout.addView(hzLabel)
        layout.addView(hzSpinner)
        layout.addView(shizukuState)
        layout.addView(requestShizuku)
        layout.addView(save)
        layout.addView(start)
        setContentView(layout)
    }
}
