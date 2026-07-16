package com.catemup.battdeck.domain

data class Battery(
    val id: Long,
    val name: String,
    val markingId: String,
    val voltage: Double,
    val sortOrder: Int,
    val isActive: Boolean,
    val isRemoved: Boolean,
    val lastUpdatedAt: Long,
    val createdAt: Long,
)

data class BatteryMarking(val id: String, val name: String, val color: Long)

fun defaultBatteryMarkings() = listOf(
    BatteryMarking("blue", "Синя", 0xFF2F80EDL),
    BatteryMarking("black", "Чорна", 0xFF252C38L),
)
enum class BatteryStatus { READY, WARNING, DANGER }
enum class AppLanguage { SYSTEM, UKRAINIAN, ENGLISH }
enum class InputError { NAME_REQUIRED, NAME_TOO_LONG, VOLTAGE_REQUIRED, INVALID_NUMBER, VOLTAGE_OUT_OF_RANGE, INTEGER_REQUIRED, COUNT_OUT_OF_RANGE, MIN_NOT_LOWER, MAX_NOT_HIGHER, DATE_REQUIRED, INVALID_DATE, DATE_IN_FUTURE }

data class AppSettings(
    val batteryCount: Int = 10,
    val minVoltage: Double = 40.2,
    val maxVoltage: Double = 50.2,
    val language: AppLanguage = AppLanguage.UKRAINIAN,
    val markings: List<BatteryMarking> = defaultBatteryMarkings(),
)
