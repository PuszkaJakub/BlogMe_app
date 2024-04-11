package com.example.projektpam

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

class BlogMeApp: Application() {
    override fun onCreate() {
        super.onCreate()
        val channel = NotificationChannel(
            "channel id",
            "Channel name",
            NotificationManager.IMPORTANCE_HIGH
        )
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}