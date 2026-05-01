package com.bartman79.elisa

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import com.bartman79.elisa.utils.MoodColorCalculator

class MoodWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
            val balance = prefs.getInt("balance", 0)
            val bgColor = MoodColorCalculator.getColor(balance)
            val textColor = getContrastColor(bgColor)
            val message = getMessageForBalance(balance, context)

            val views = RemoteViews(context.packageName, R.layout.widget_mood)
            views.setTextViewText(R.id.tvMoodNumber, if (balance > 0) "+$balance" else balance.toString())
            views.setTextViewText(R.id.tvMoodMessage, message)
            views.setTextColor(R.id.tvMoodNumber, textColor)
            views.setTextColor(R.id.tvMoodMessage, textColor)
            views.setInt(R.id.widgetRoot, "setBackgroundColor", bgColor)

            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.widgetRoot, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun getContrastColor(color: Int): Int {
            val red = Color.red(color)
            val green = Color.green(color)
            val blue = Color.blue(color)
            val brightness = (0.299 * red + 0.587 * green + 0.114 * blue) / 255
            return if (brightness > 0.5) Color.BLACK else Color.WHITE
        }

        private fun getMessageForBalance(balance: Int, context: Context): String {
            val messages = context.resources.getStringArray(R.array.widget_messages)
            return when (balance) {
                in Int.MIN_VALUE..-71 -> messages[0] // Совсем тяжело
                in -70..-41           -> messages[1] // Трудно
                in -40..-11           -> messages[2] // Усталость
                in -10..10            -> messages[3] // Ровно (штиль)
                in 11..30             -> messages[4] // Неплохо
                in 31..50             -> messages[5] // Хорошо
                in 51..75             -> messages[6] // Очень хорошо
                in 76..95             -> messages[7] // Отлично
                else                  -> messages[8] // Великолепно/Пик
            }
        }
    }
}