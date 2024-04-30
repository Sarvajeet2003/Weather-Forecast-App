package com.kamesh.weatherapplication.Notifiaction
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.kamesh.weatherapplication.R
object WeatherNotification {

    private const val CHANNEL_ID = "WeatherChannel"

    @SuppressLint("RemoteViewLayout")
    fun showNotification(context: Context, currentTemp: Double?) {
        val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Create NotificationChannel if running on Android Oreo or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Weather Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
            println(channel)
        }

        // Create custom notification layout
        val notificationLayout = RemoteViews(context.packageName, R.layout.notification_layout)
        notificationLayout.setTextViewText(R.id.notification_content, "Current temperature: $currentTemp°C")
        notificationLayout.setTextViewText(R.id.notification_title, "Current Temperature")

        // Create notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setCustomContentView(notificationLayout) // Set custom layout
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Set visibility
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        // Show the notification
        notificationManager.notify(0, notification)
    }

}