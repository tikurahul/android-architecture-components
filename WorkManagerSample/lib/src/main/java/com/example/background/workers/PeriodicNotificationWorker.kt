package com.example.background.workers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.example.background.library.R
import kotlinx.coroutines.delay

class PeriodicNotificationWorker(context: Context, parameters: WorkerParameters) :
        CoroutineWorker(context, parameters) {

    private val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private var progress: Data = Data.EMPTY

    override suspend fun doWork(): Result {
        val notificationId = inputData.getInt(InputNotificationId, NotificationId)
        val delayTime = inputData.getLong(InputDelayTime, Delay)
        // Run in the context of a Foreground service
        setForeground(getForegroundInfo(notificationId))
        val range = 20
        for (i in 1..range) {
            if (!isStopped) {
                delay(delayTime)
                progress = workDataOf(Progress to i * (100 / range))
                setProgress(progress)
                setForeground(getForegroundInfo(notificationId))
            }
        }
        return Result.success()
    }

    private fun getForegroundInfo(notificationId: Int): ForegroundInfo {
        val percent = progress.getInt(Progress, 0)
        val id = applicationContext.getString(R.string.channel_id)
        val title = applicationContext.getString(R.string.notification_title)
        val content = "Progress ($percent) %"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }

        val notification = NotificationCompat.Builder(applicationContext, id)
                .setContentTitle(title)
                .setTicker(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_work_notification)
                .setOngoing(true)
                .build()

        return ForegroundInfo(notificationId, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        val id = applicationContext.getString(R.string.channel_id)
        val name = applicationContext.getString(R.string.channel_name)
        val description = applicationContext.getString(R.string.channel_description)
        val channel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_LOW)
        channel.description = description
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        // The Worker's unique name
        const val UNIQUE_NAME = "PeriodicNotificationWorker"

        private const val NotificationId = 10
        private const val Delay = 1000L
        private const val Progress = "Progress"
        const val InputNotificationId = "NotificationId"
        const val InputDelayTime = "DelayTime"
    }
}
