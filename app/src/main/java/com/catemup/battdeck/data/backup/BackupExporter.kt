package com.catemup.battdeck.data.backup

import com.catemup.battdeck.data.AppData
import org.json.JSONArray
import org.json.JSONObject

object BackupExporter {
    fun export(data: AppData, exportedAt: Long = System.currentTimeMillis()): String = JSONObject().apply {
        put("schemaVersion", 1)
        put("app", "BattDeck")
        put("exportedAt", exportedAt)
        put("settings", JSONObject().apply {
            put("batteryCount", data.settings.batteryCount)
            put("minVoltage", data.settings.minVoltage)
            put("maxVoltage", data.settings.maxVoltage)
            put("language", if (data.settings.language == com.catemup.battdeck.domain.AppLanguage.ENGLISH) "EN" else "UK")
            put("markings", JSONArray().apply { data.settings.markings.forEach { marking ->
                put(JSONObject().apply { put("name", marking.name); put("color", marking.color) })
            } })
        })
        put("batteries", JSONArray().apply { data.batteries.forEach { battery ->
            put(JSONObject().apply {
                put("name", battery.name)
                put("markingIndex", battery.markingIndex)
                put("voltage", battery.voltage); put("sortOrder", battery.sortOrder)
                put("isActive", battery.isActive); put("isRemoved", battery.isRemoved)
                put("lastUpdatedAt", battery.lastUpdatedAt); put("createdAt", battery.createdAt)
            })
        } })
    }.toString(2)
}
