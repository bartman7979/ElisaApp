package com.bartman79.elisa

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bartman79.elisa.utils.PersonalityManager
import com.bartman79.elisa.utils.PersonalityType
import kotlinx.coroutines.flow.first
import java.util.Calendar
import android.util.Log
import kotlin.random.Random

class NotificationWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {

        val prefs = applicationContext.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val notificationsEnabled = prefs.getBoolean("notifications_enabled", true)
        if (!notificationsEnabled) {
            Log.d("NotificationWorker", "Notifications disabled, skipping")
            return Result.success()
        }
        // Логирование для отладки
        Log.d("NotificationWorker", "Worker started at ${System.currentTimeMillis()}")
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
       Log.d("NotificationWorker", "Current hour: $currentHour")
        if (currentHour !in 8..20) {
            android.util.Log.d("NotificationWorker", "Outside allowed hours (8-20), skipping")
            return Result.success()
        }

        val personalityManager = PersonalityManager(applicationContext)
        val personality = personalityManager.currentPersonality.first()
        val phrases = getPhrases(personality)

        if (phrases.isNotEmpty()) {
            // Предотвращаем повторение последней фразы
            val prefs = applicationContext.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
            val lastPhraseKey = "last_phrase_${personality.name}"
            val lastPhrase = prefs.getString(lastPhraseKey, null)

            val availablePhrases = if (lastPhrase != null && phrases.size > 1) {
                phrases.filter { it != lastPhrase }
            } else {
                phrases.toList()
            }

            val randomPhrase = availablePhrases.random()
            prefs.edit().putString(lastPhraseKey, randomPhrase).apply()

            android.util.Log.d("NotificationWorker", "Sending notification: $randomPhrase")
            showNotification(randomPhrase)
        } else {
            android.util.Log.d("NotificationWorker", "No phrases found for personality $personality")
        }

        return Result.success()
    }

    private fun getPhrases(personality: PersonalityType): Array<String> {
        return when (personality) {
            PersonalityType.FRIEND -> applicationContext.resources.getStringArray(R.array.encouragements_friend)
            PersonalityType.SISTER -> applicationContext.resources.getStringArray(R.array.encouragements_sister)
            PersonalityType.COACH -> applicationContext.resources.getStringArray(R.array.encouragements_coach)
        }
    }

    private fun showNotification(message: String) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "eliza_channel",
                "Элиза",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Уведомления от Элизы"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, "eliza_channel")
            .setContentTitle("Элиза")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}