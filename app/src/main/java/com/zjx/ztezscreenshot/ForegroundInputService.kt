package com.zjx.ztezscreenshot

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Process
import androidx.core.app.NotificationCompat
import java.util.concurrent.atomic.AtomicBoolean

class ForegroundInputService : Service() {
    private val queue = LowLatencyInputQueue()
    private lateinit var dispatcher: LowLatencyDispatcher
    private lateinit var backend: InputBackend
    private val demoRunning = AtomicBoolean(false)
    @Volatile private var demoThread: Thread? = null

    override fun onCreate() {
        super.onCreate()
        val config = AppConfig(this)
        createChannel()
        startForeground(1, buildNotification("Low-latency pipeline active"))

        backend = BackendFactory.create(this, config.backendMode())
        dispatcher = LowLatencyDispatcher(
            backend = backend,
            queue = queue,
            pollHz = config.mousePollHz(),
        )
        dispatcher.start()
        startDemoTransport()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopDemoTransport()
        dispatcher.shutdown()
        BackendFactory.shutdown(backend)
        super.onDestroy()
    }

    private fun startDemoTransport() {
        if (!demoRunning.compareAndSet(false, true)) return
        demoThread = Thread({
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_DISPLAY)
            var phase = 0
            while (demoRunning.get()) {
                val delta = if (phase % 2 == 0) 1.0f else -1.0f
                queue.offer(MouseMoveFrame(delta, 0f, System.nanoTime()))
                if (phase % 250 == 0) queue.offer(MouseButtonFrame(1, true, System.nanoTime()))
                if (phase % 250 == 1) queue.offer(MouseButtonFrame(1, false, System.nanoTime()))
                phase++
                try {
                    Thread.sleep(1)
                } catch (_: InterruptedException) {
                    break
                }
            }
        }, "ggll-demo-transport").apply { start() }
    }

    private fun stopDemoTransport() {
        if (!demoRunning.compareAndSet(true, false)) return
        demoThread?.interrupt()
        demoThread = null
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "ggmouse_ll",
                "GG Mouse Low Latency",
                NotificationManager.IMPORTANCE_LOW,
            )
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(text: String): Notification = NotificationCompat.Builder(this, "ggmouse_ll")
        .setContentTitle("GG Mouse Low Latency")
        .setContentText(text)
        .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
        .setOngoing(true)
        .build()
}
