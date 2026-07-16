package com.catemup.battdeck.domain

data class Battery(
    val id: Long,
    val number: Int,
    val type: BatteryType,
    val voltage: Double,
    val sortOrder: Int,
    val isActive: Boolean,
    val isRemoved: Boolean,
    val lastUpdatedAt: Long,
    val createdAt: Long,
)

enum class BatteryType { BLUE, BLACK }
enum class BatteryStatus { READY, WARNING, LOW, DANGER }

data class AppSettings(
    val batteryCount: Int = 10,
    val minVoltage: Double = 40.2,
    val maxVoltage: Double = 50.2,
)
