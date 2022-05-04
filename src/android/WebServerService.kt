package webserverplugin

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class WebServerService : Service() {

    private val server by lazy {
        WebServer.getInstance(applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "start") {
            startInForeground()
            server.start()
        } else {
            server.stop()
            stopForeground(true)
            stopSelf()
        }
        return START_STICKY
    }

    private fun startInForeground() {
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("WebServer")
            .setContentText("running...")
            .build()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = NOTIFICATION_CHANNEL_DESC
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onDestroy() {
        server.stop()
        stopForeground(true)
        stopSelf()
        super.onDestroy()
    }

    companion object {
        const val NOTIFICATION_ID = 1
        const val NOTIFICATION_CHANNEL_ID = "notification_channel_id"
        const val NOTIFICATION_CHANNEL_NAME = "notification_channel_name"
        const val NOTIFICATION_CHANNEL_DESC = "notification_channel_desc"
    }
}
