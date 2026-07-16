package com.catemup.battdeck.data

import android.content.Context
import android.util.AtomicFile
import com.catemup.battdeck.domain.AppSettings
import com.catemup.battdeck.domain.AppLanguage
import com.catemup.battdeck.domain.Battery
import com.catemup.battdeck.domain.BatteryRules
import com.catemup.battdeck.domain.BatteryMarking
import com.catemup.battdeck.domain.defaultBatteryMarkings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.Locale

data class AppData(
    val settings: AppSettings = AppSettings(),
    val batteries: List<Battery> = emptyList(),
)

/** Stores the complete application state in one private, atomically-written JSON file. */
class JsonRepository(context: Context) {
    private val file = AtomicFile(File(context.filesDir, FILE_NAME))
    private val mutex = Mutex()
    private val mutableData = MutableStateFlow(AppData())
    val data: StateFlow<AppData> = mutableData.asStateFlow()

    suspend fun initialize() = mutex.withLock {
        val loaded = withContext(Dispatchers.IO) {
            if (file.baseFile.exists()) runCatching { decode(file.readFully().decodeToString()) }.getOrNull() else null
        }
        val initial = loaded?.normalized() ?: defaultData()
        write(initial)
        mutableData.value = initial
    }

    suspend fun saveBattery(battery: Battery) = update { current ->
        current.copy(batteries = current.batteries.map { if (it.id == battery.id) battery else it })
    }

    suspend fun setActive(id: Long) = update { current ->
        val deactivate = current.batteries.firstOrNull { it.id == id }?.isActive == true
        current.copy(batteries = current.batteries.map {
            it.copy(isActive = !deactivate && !it.isRemoved && it.id == id)
        })
    }

    suspend fun moveReady(id: Long, direction: Int) = update { current ->
        val ready = current.batteries
            .filterNot { it.isRemoved }
            .filter { BatteryRules.percent(it.voltage, current.settings.minVoltage, current.settings.maxVoltage) >= 95 }
            .sortedBy { it.sortOrder }
        val from = ready.indexOfFirst { it.id == id }
        val to = from + direction
        if (from < 0 || to !in ready.indices) return@update current
        val first = ready[from]
        val second = ready[to]
        current.copy(batteries = current.batteries.map {
            when (it.id) {
                first.id -> it.copy(sortOrder = second.sortOrder)
                second.id -> it.copy(sortOrder = first.sortOrder)
                else -> it
            }
        })
    }

    suspend fun reset(id: Long) = update { current ->
        val now = System.currentTimeMillis()
        current.copy(batteries = current.batteries.map {
            if (it.id == id) it.copy(voltage = current.settings.minVoltage, isActive = false, lastUpdatedAt = now) else it
        })
    }

    suspend fun remove(id: Long) = update { current ->
        current.copy(batteries = current.batteries.map {
            if (it.id == id) it.copy(isRemoved = true, isActive = false) else it
        })
    }

    suspend fun saveSettings(settings: AppSettings, originalMarkingIndices: List<Int>) = update { current ->
        current.copy(
            settings = settings,
            batteries = current.batteries.map { battery ->
                battery.copy(markingIndex = originalMarkingIndices.indexOf(battery.markingIndex).takeIf { it >= 0 } ?: 0)
            },
        ).reconcileBatteryCount()
    }

    suspend fun replaceAll(data: AppData) = mutex.withLock {
        val next = data.normalized()
        write(next)
        mutableData.value = next
    }

    private suspend fun update(transform: (AppData) -> AppData) = mutex.withLock {
        val next = transform(mutableData.value).normalized()
        write(next)
        mutableData.value = next
    }

    private suspend fun write(value: AppData) = withContext(Dispatchers.IO) {
        val stream = file.startWrite()
        try {
            stream.write(encode(value).toByteArray())
            file.finishWrite(stream)
        } catch (error: Throwable) {
            file.failWrite(stream)
            throw error
        }
    }

    private fun defaultData(): AppData {
        val settings = AppSettings(language = defaultLanguage())
        val now = System.currentTimeMillis()
        return AppData(settings, (1..settings.batteryCount).map { number ->
            Battery(number.toLong(), String.format(Locale.US, "%02d", number), 0, settings.maxVoltage, number - 1, number == 1, false, now, now)
        })
    }

    private fun AppData.reconcileBatteryCount(): AppData {
        val visible = batteries.filterNot { it.isRemoved }.sortedBy { it.sortOrder }
        if (visible.size == settings.batteryCount) return this
        if (visible.size > settings.batteryCount) {
            val hiddenIds = visible.drop(settings.batteryCount).mapTo(mutableSetOf()) { it.id }
            return copy(batteries = batteries.map { if (it.id in hiddenIds) it.copy(isRemoved = true, isActive = false) else it })
        }
        val now = System.currentTimeMillis()
        val amount = settings.batteryCount - visible.size
        val nextId = (batteries.maxOfOrNull { it.id } ?: 0L) + 1
        val nextOrder = (batteries.maxOfOrNull { it.sortOrder } ?: -1) + 1
        val added = (0 until amount).map { index ->
            val defaultNumber = nextOrder + index + 1
            Battery(nextId + index, String.format(Locale.US, "%02d", defaultNumber), 0, settings.maxVoltage, nextOrder + index, false, false, now, now)
        }
        return copy(batteries = batteries + added)
    }

    private fun AppData.normalized(): AppData {
        val safeMin = if (settings.minVoltage.isFinite()) settings.minVoltage else 40.2
        val safeMax = if (settings.maxVoltage.isFinite() && settings.maxVoltage > safeMin) {
            settings.maxVoltage
        } else {
            maxOf(50.2, safeMin + 0.1)
        }
        val safeMarkings = settings.markings.filter { it.name.isNotBlank() }.ifEmpty { defaultBatteryMarkings() }
        val safeSettings = settings.copy(
            batteryCount = settings.batteryCount.coerceIn(1, 50),
            minVoltage = safeMin,
            maxVoltage = safeMax,
            language = if (settings.language == AppLanguage.SYSTEM) defaultLanguage() else settings.language,
            markings = safeMarkings,
        )
        val activeId = batteries.firstOrNull { it.isActive && !it.isRemoved }?.id
        return copy(settings = safeSettings, batteries = batteries.map {
            it.copy(isActive = it.id == activeId, markingIndex = it.markingIndex.takeIf { index -> index in safeMarkings.indices } ?: 0)
        }).reconcileBatteryCount()
    }

    private fun encode(value: AppData): String = JSONObject().apply {
        put("schemaVersion", 5)
        put("settings", JSONObject().apply {
            put("batteryCount", value.settings.batteryCount)
            put("minVoltage", value.settings.minVoltage)
            put("maxVoltage", value.settings.maxVoltage)
            put("language", value.settings.language.toStorageCode())
            put("markings", JSONArray().apply { value.settings.markings.forEach { marking ->
                put(JSONObject().apply { put("name", marking.name); put("color", marking.color) })
            } })
        })
        put("batteries", JSONArray().apply { value.batteries.forEach { battery ->
            put(JSONObject().apply {
                put("id", battery.id); put("name", battery.name); put("markingIndex", battery.markingIndex)
                put("voltage", battery.voltage); put("sortOrder", battery.sortOrder); put("isActive", battery.isActive)
                put("isRemoved", battery.isRemoved); put("lastUpdatedAt", battery.lastUpdatedAt); put("createdAt", battery.createdAt)
            })
        } })
    }.toString(2)

    private fun decode(json: String): AppData {
        val root = JSONObject(json)
        val settingsJson = root.getJSONObject("settings")
        val markingArray = settingsJson.optJSONArray("markings")
        val legacyMarkingIds = markingArray?.let { array ->
            buildList { for (index in 0 until array.length()) add(array.getJSONObject(index).optString("id")) }
        } ?: emptyList()
        val markings = markingArray?.let { markingArray ->
            buildList { for (index in 0 until markingArray.length()) { val item = markingArray.getJSONObject(index); add(BatteryMarking(item.getString("name"), item.getLong("color"))) } }
        } ?: defaultBatteryMarkings()
        val settings = AppSettings(
            settingsJson.getInt("batteryCount"), settingsJson.getDouble("minVoltage"), settingsJson.getDouble("maxVoltage"),
            parseLanguage(settingsJson.optString("language")),
            markings,
        )
        val array = root.getJSONArray("batteries")
        val batteries = buildList {
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                add(Battery(
                    id = item.getLong("id"),
                    name = item.optString("name").ifBlank { String.format(Locale.US, "%02d", item.optInt("number", index + 1)) },
                    markingIndex = if (item.has("markingIndex")) item.optInt("markingIndex") else {
                        val legacyId = item.optString("markingId")
                        legacyMarkingIds.indexOf(legacyId).takeIf { it >= 0 } ?: when (legacyId) {
                            "black" -> 1
                            "blue" -> 0
                            else -> if (item.optString("type") == "BLACK") 1 else 0
                        }
                    },
                    voltage = item.getDouble("voltage"), sortOrder = item.getInt("sortOrder"),
                    isActive = item.optBoolean("isActive"), isRemoved = item.optBoolean("isRemoved"),
                    lastUpdatedAt = item.getLong("lastUpdatedAt"), createdAt = item.getLong("createdAt"),
                ))
            }
        }
        return AppData(settings, batteries)
    }

    private fun defaultLanguage(): AppLanguage =
        if (Locale.getDefault().language == "en") AppLanguage.ENGLISH else AppLanguage.UKRAINIAN

    private fun AppLanguage.toStorageCode(): String = if (this == AppLanguage.ENGLISH) "EN" else "UK"

    private fun parseLanguage(value: String): AppLanguage = when (value) {
        "EN", AppLanguage.ENGLISH.name -> AppLanguage.ENGLISH
        "UK", AppLanguage.UKRAINIAN.name -> AppLanguage.UKRAINIAN
        else -> defaultLanguage()
    }

    companion object { const val FILE_NAME = "battdeck.json" }
}
