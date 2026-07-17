package com.catemup.battdeck.data.backup

import com.catemup.battdeck.data.AppData
import com.catemup.battdeck.domain.*
import org.json.JSONObject

object BackupImporter {
    fun parse(text: String): ImportResult {
        val root = try { JSONObject(text) } catch (_: Throwable) { return ImportResult.InvalidJson }
        if (!root.has("schemaVersion") || (root.has("app") && root.optString("app") != "BattDeck")) return ImportResult.NotBattDeck
        if (root.optInt("schemaVersion", -1) != 1) return ImportResult.UnsupportedVersion
        val array = root.optJSONArray("batteries") ?: return ImportResult.NotBattDeck
        val settingsJson = root.optJSONObject("settings") ?: return ImportResult.NotBattDeck
        return try {
            val batteryCount = settingsJson.getInt("batteryCount")
            val minVoltage = settingsJson.getDouble("minVoltage")
            val maxVoltage = settingsJson.getDouble("maxVoltage")
            if (batteryCount !in 1..50 || !minVoltage.isFinite() || !maxVoltage.isFinite() || minVoltage >= maxVoltage) return ImportResult.InvalidBatteries
            val markingItems = settingsJson.optJSONArray("markings")
            val legacyMarkingIds = markingItems?.let { items -> buildList { for (index in 0 until items.length()) add(items.getJSONObject(index).optString("id")) } } ?: emptyList()
            val markings = markingItems?.let { items ->
                buildList { for (index in 0 until items.length()) { val item = items.getJSONObject(index); add(BatteryMarking(item.getString("name").trim(), item.getLong("color"))) } }
            }?.filter { it.name.isNotBlank() }?.ifEmpty { null } ?: defaultBatteryMarkings()
            val language = when (settingsJson.optString("language")) {
                "EN", AppLanguage.ENGLISH.name -> AppLanguage.ENGLISH
                else -> AppLanguage.UKRAINIAN
            }
            val imported = buildList {
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    val name = item.getString("name").trim()
                    val voltage = item.getDouble("voltage")
                    if (name.isEmpty() || name.length > 32 || !voltage.isFinite()) return ImportResult.InvalidBatteries
                    val legacyType = item.optString("type")
                    val markingIndex = when {
                        item.has("markingIndex") -> item.optInt("markingIndex", -1)
                        item.optString("markingId").isNotBlank() -> legacyMarkingIds.indexOf(item.optString("markingId")).takeIf { it >= 0 }
                            ?: when (item.optString("markingId")) { "blue" -> 0; "black" -> 1; else -> -1 }
                        else -> when (legacyType) { "BLUE" -> 0; "BLACK" -> 1; else -> -1 }
                    }
                    if (markingIndex !in markings.indices) return ImportResult.InvalidBatteries
                    add(Battery(
                        id = index + 1L, name = name, markingIndex = markingIndex, voltage = voltage,
                        sortOrder = item.optInt("sortOrder", index), isActive = item.optBoolean("isActive"),
                        isRemoved = item.optBoolean("isRemoved"), lastUpdatedAt = item.optLong("lastUpdatedAt", System.currentTimeMillis()),
                        createdAt = item.optLong("createdAt", System.currentTimeMillis()),
                    ))
                }
            }
            val firstActiveId = imported.filterNot { it.isRemoved }.filter { it.isActive }.minByOrNull { it.sortOrder }?.id
            val batteries = imported.map { it.copy(isActive = it.id == firstActiveId) }
            val settings = AppSettings(batteryCount, minVoltage, maxVoltage, language, markings)
            ImportResult.Success(ImportPreview(AppData(settings, batteries), batteryCount = batteries.size, exportedAt = root.optLong("exportedAt").takeIf { root.has("exportedAt") }, hasSettings = true))
        } catch (_: Throwable) { ImportResult.InvalidBatteries }
    }
}
