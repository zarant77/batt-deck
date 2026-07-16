package com.catemup.battdeck.domain

import kotlin.math.roundToInt

object BatteryRules {
    fun percent(voltage: Double, minVoltage: Double, maxVoltage: Double): Int {
        if (maxVoltage <= minVoltage) return 0
        return (((voltage - minVoltage) / (maxVoltage - minVoltage)) * 100).roundToInt().coerceIn(0, 100)
    }

    fun status(percent: Int): BatteryStatus = when {
        percent >= 95 -> BatteryStatus.READY
        percent >= 50 -> BatteryStatus.WARNING
        percent > 0 -> BatteryStatus.LOW
        else -> BatteryStatus.DANGER
    }

    fun clampVoltage(voltage: Double, settings: AppSettings): Double =
        voltage.coerceIn(settings.minVoltage, settings.maxVoltage)

    fun lastUpdatedText(isActive: Boolean, timestamp: Long, now: Long = System.currentTimeMillis()): String {
        if (isActive) return "АКТИВНА"
        val days = ((now - timestamp).coerceAtLeast(0) / 86_400_000L).toInt()
        return when (days) {
            0 -> "СЬОГОДНІ"
            1 -> "1 ДЕНЬ ТОМУ"
            in 2..4 -> "$days ДНІ ТОМУ"
            else -> "$days ДНІВ ТОМУ"
        }
    }
}
