package com.catemup.battdeck.domain

import kotlin.math.roundToInt
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

object BatteryRules {
    fun percent(voltage: Double, minVoltage: Double, maxVoltage: Double): Int {
        if (maxVoltage <= minVoltage) return 0
        return (((voltage - minVoltage) / (maxVoltage - minVoltage)) * 100).roundToInt().coerceIn(0, 100)
    }

    fun status(percent: Int): BatteryStatus = when {
        percent >= 95 -> BatteryStatus.READY
        percent >= 50 -> BatteryStatus.WARNING
        else -> BatteryStatus.DANGER
    }

    fun clampVoltage(voltage: Double, settings: AppSettings): Double =
        voltage.coerceIn(settings.minVoltage, settings.maxVoltage)

    fun daysSinceUpdate(timestamp: Long, now: Long = System.currentTimeMillis()): Int? =
        if (timestamp <= 0) null else ((now - timestamp).coerceAtLeast(0) / 86_400_000L).toInt()
}

object InputRules {
    const val MAX_NAME_LENGTH = 32
    const val MIN_BATTERY_COUNT = 1
    const val MAX_BATTERY_COUNT = 50

    fun normalizeDecimal(value: String): String = value.trim().replace(',', '.')
    fun decimalOrNull(value: String): Double? = normalizeDecimal(value).toDoubleOrNull()?.takeIf { it.isFinite() }
    fun integerOrNull(value: String): Int? = value.trim().toIntOrNull()
    fun isDecimalDraft(value: String): Boolean = value.isEmpty() || value.matches(Regex("[0-9]*([.,][0-9]*)?"))
    fun isIntegerDraft(value: String): Boolean = value.isEmpty() || value.all(Char::isDigit)
    fun normalizedName(value: String): String = value.trim()
    fun nameError(value: String): InputError? = when {
        normalizedName(value).isEmpty() -> InputError.NAME_REQUIRED
        normalizedName(value).length > MAX_NAME_LENGTH -> InputError.NAME_TOO_LONG
        else -> null
    }

    fun voltageError(value: String, min: Double, max: Double): InputError? {
        val voltage = decimalOrNull(value)
        return when {
            value.isBlank() -> InputError.VOLTAGE_REQUIRED
            voltage == null -> InputError.INVALID_NUMBER
            voltage !in min..max -> InputError.VOLTAGE_OUT_OF_RANGE
            else -> null
        }
    }

    fun batteryCountError(value: String): InputError? {
        val count = integerOrNull(value)
        return when {
            count == null -> InputError.INTEGER_REQUIRED
            count !in MIN_BATTERY_COUNT..MAX_BATTERY_COUNT -> InputError.COUNT_OUT_OF_RANGE
            else -> null
        }
    }

    fun minVoltageError(value: String, maxValue: String): InputError? {
        val min = decimalOrNull(value)
        val max = decimalOrNull(maxValue)
        return when {
            min == null -> InputError.INVALID_NUMBER
            max != null && min >= max -> InputError.MIN_NOT_LOWER
            else -> null
        }
    }

    fun maxVoltageError(value: String, minValue: String): InputError? {
        val max = decimalOrNull(value)
        val min = decimalOrNull(minValue)
        return when {
            max == null -> InputError.INVALID_NUMBER
            min != null && max <= min -> InputError.MAX_NOT_HIGHER
            else -> null
        }
    }

    private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.uuuu").withResolverStyle(ResolverStyle.STRICT)

    fun isDateDraft(value: String): Boolean = value.length <= 10 && value.all { it.isDigit() || it == '.' }
    fun formatDateInput(value: String): String {
        val digits = value.filter(Char::isDigit).take(8)
        return when {
            digits.length <= 2 -> digits
            digits.length <= 4 -> "${digits.take(2)}.${digits.drop(2)}"
            else -> "${digits.take(2)}.${digits.substring(2, 4)}.${digits.drop(4)}"
        }
    }
    fun dateOrNull(value: String): LocalDate? = runCatching { LocalDate.parse(value.trim(), dateFormatter) }.getOrNull()
    fun formatDate(timestamp: Long): String =
        if (timestamp <= 0) "" else dateFormatter.format(java.time.Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate())
    fun dateTimestamp(value: String): Long? = dateOrNull(value)?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
    fun dateError(value: String, today: LocalDate = LocalDate.now()): InputError? {
        val date = dateOrNull(value)
        return when {
            value.isBlank() -> InputError.DATE_REQUIRED
            date == null -> InputError.INVALID_DATE
            date.isAfter(today) -> InputError.DATE_IN_FUTURE
            else -> null
        }
    }
}
