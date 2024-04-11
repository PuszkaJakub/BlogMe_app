package com.example.projektpam

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat

fun sendNotification(context: Context, notificationType: String) {
    val channelID = "10000"
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val bitmapLargeIcon: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.logo)

    val builder = NotificationCompat.Builder(context, channelID)
        .setContentTitle("Blog.me")
        .setSmallIcon(R.drawable.logo)
        .setLargeIcon(bitmapLargeIcon)

    if (notificationType == "public") {
        builder
            .setContentText("New public post")
            .setStyle(NotificationCompat.BigTextStyle().bigText("You just recieved a new public post"))
    } else {
        builder
            .setContentText("New private message")
            .setStyle(NotificationCompat.BigTextStyle().bigText("You just recieved a new private message"))
    }

    val channel = NotificationChannel(channelID, "Blog.me", NotificationManager.IMPORTANCE_DEFAULT).apply {
        description = "New Post recieved"
    }
    notificationManager.createNotificationChannel(channel)

    channel.enableVibration(true)

    builder.setChannelId(channelID)

    val notification = builder.build()
    notificationManager.notify(1000, notification)
}
