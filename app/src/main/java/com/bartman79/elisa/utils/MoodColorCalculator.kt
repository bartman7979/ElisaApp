package com.bartman79.elisa.utils

import android.graphics.Color
import kotlin.math.roundToInt

object MoodColorCalculator {
    // Диапазон баланса (можешь менять)
    private const val MIN_BALANCE = -100
    private const val MAX_BALANCE = 100

    // Крайние цвета в HSV
    private val COLD_HSV = floatArrayOf(200f, 1.0f, 0.9f)   // яркий синий
    private val NEUTRAL_HSV = floatArrayOf(40f, 0.8f, 0.95f) // тёплый персиковый
    private val WARM_HSV = floatArrayOf(30f, 1.0f, 1.0f)     // яркий оранжевый

    fun getColor(balance: Int): Int {
        val clamped = balance.coerceIn(MIN_BALANCE, MAX_BALANCE)
        val fraction = (clamped - MIN_BALANCE).toFloat() / (MAX_BALANCE - MIN_BALANCE)

        return if (fraction <= 0.5f) {
            // Интерполяция от холодного к нейтральному
            val f = fraction / 0.5f
            val h = COLD_HSV[0] + (NEUTRAL_HSV[0] - COLD_HSV[0]) * f
            val s = COLD_HSV[1] + (NEUTRAL_HSV[1] - COLD_HSV[1]) * f
            val v = COLD_HSV[2] + (NEUTRAL_HSV[2] - COLD_HSV[2]) * f
            Color.HSVToColor(floatArrayOf(h, s, v))
        } else {
            // Интерполяция от нейтрального к тёплому
            val f = (fraction - 0.5f) / 0.5f
            val h = NEUTRAL_HSV[0] + (WARM_HSV[0] - NEUTRAL_HSV[0]) * f
            val s = NEUTRAL_HSV[1] + (WARM_HSV[1] - NEUTRAL_HSV[1]) * f
            val v = NEUTRAL_HSV[2] + (WARM_HSV[2] - NEUTRAL_HSV[2]) * f
            Color.HSVToColor(floatArrayOf(h, s, v))
        }
    }
}