package com.bartman79.elisa

import android.app.Application
import androidx.work.*
import java.util.concurrent.TimeUnit

class ElizaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        scheduleNotifications()
    }

    private fun scheduleNotifications() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        // Периодичность 1 час (минимальный интервал для PeriodicWorkRequest — 15 минут)
        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
            1, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "eliza_notifications",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}