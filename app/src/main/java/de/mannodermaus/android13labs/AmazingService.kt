package de.mannodermaus.android13labs

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.NotificationManagerCompat.IMPORTANCE_LOW
import androidx.core.app.ServiceCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val serviceNotifChannel = "service-notifications"
private const val serviceNotifId = 1337

/**
 * Example of a foreground service to test Android 13's notification permission.
 */
class AmazingService : Service() {

    private var scope: CoroutineScope? = null

    override fun onBind(p0: Intent?): IBinder {
        return Binder()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("Service onStartCommand()")

        ensureNotificationChannel()

        val notification = NotificationCompat.Builder(this, serviceNotifChannel)
            .setContentTitle("Hello from FG service")
            .setContentText("Content goes here")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        startForeground(serviceNotifId, notification)

        // Example work of the service
        startAsyncWork()

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        stopAsyncWork()

        println("Service onDestroy()")
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
    }

    /* Private */

    private fun ensureNotificationChannel() {
        val manager = NotificationManagerCompat.from(this)
        if (manager.getNotificationChannel(serviceNotifChannel) == null) {
            manager.createNotificationChannel(
                NotificationChannelCompat.Builder(serviceNotifChannel, IMPORTANCE_LOW)
                    .setName("FG Service Notifications")
                    .build()
            )
        }
    }

    private fun startAsyncWork() {
        CoroutineScope(Dispatchers.IO)
            .also { this.scope = it }
            .launch {
                while (true) {
                    delay(1000)
                    println("Service: Doing some work")
                }
            }
    }

    private fun stopAsyncWork() {
        this.scope?.cancel()
        this.scope = null
    }
}