package com.example.subzero.global

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    fun scheduleReminderNotification(context: Context, title: String, body: String, delayInMillis: Long) {
        val data = Data.Builder()
            .putString("title", title)
            .putString("body", body)
            .build()

        val request = OneTimeWorkRequestBuilder<Notification>()
            .setInitialDelay(delayInMillis, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }
}