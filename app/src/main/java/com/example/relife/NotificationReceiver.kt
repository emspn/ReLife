package com.example.relife

import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskName = intent.getStringExtra("task_name") ?: "Task"
        val taskTime = intent.getStringExtra("task_time") ?: "Time"

        val notification = NotificationCompat.Builder(context, "task_channel")
            .setSmallIcon(R.drawable.baseline_10k_24) // Your app icon
            .setContentTitle("Reminder: $taskName")
            .setContentText("It's time for your task: $taskName at $taskTime")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(1, notification)
    }
}
