package com.catemup.battdeck.data

import android.content.Context
import android.util.AtomicFile
import com.catemup.battdeck.domain.AppSettings
import com.catemup.battdeck.domain.Battery
import com.catemup.battdeck.domain.BatteryType
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
        current.copy(batteries = current.batteries.map { it.copy(isActive = !it.isRemoved && it.id == id) })
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

    suspend fun saveSettings(settings: AppSettings) = update { current ->
        current.copy(settings = settings).reconcileBatteryCount()
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
        val settings = AppSettings()
        val now = System.currentTimeMillis()
        return AppData(settings, (1..settings.batteryCount).map { number ->
            Battery(number.toLong(), number, BatteryType.BLUE, settings.maxVoltage, number - 1, number == 1, false, now, now)
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
        val nextNumber = (batteries.maxOfOrNull { it.number } ?: 0) + 1
        val nextOrder = (batteries.maxOfOrNull { it.sortOrder } ?: -1) + 1
        val added = (0 until amount).map { index ->
            Battery(nextId + index, nextNumber + index, BatteryType.BLUE, settings.maxVoltage, nextOrder + index, false, false, now, now)
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
        val safeSettings = settings.copy(
            batteryCount = settings.batteryCount.coerceIn(1, 99),
            minVoltage = safeMin,
            maxVoltage = safeMax,
        )
        val activeId = batteries.firstOrNull { it.isActive && !it.isRemoved }?.id
        return copy(settings = safeSettings, batteries = batteries.map { it.copy(isActive = it.id == activeId) }).reconcileBatteryCount()
    }

    private fun encode(value: AppData): String = JSONObject().apply {
        put("schemaVersion", 1)
        put("settings", JSONObject().apply {
            put("batteryCount", value.settings.batteryCount)
            put("minVoltage", value.settings.minVoltage)
            put("maxVoltage", value.settings.maxVoltage)
        })
        put("batteries", JSONArray().apply { value.batteries.forEach { battery ->
            put(JSONObject().apply {
                put("id", battery.id); put("number", battery.number); put("type", battery.type.name)
                put("voltage", battery.voltage); put("sortOrder", battery.sortOrder); put("isActive", battery.isActive)
                put("isRemoved", battery.isRemoved); put("lastUpdatedAt", battery.lastUpdatedAt); put("createdAt", battery.createdAt)
            })
        } })
    }.toString(2)

    private fun decode(json: String): AppData {
        val root = JSONObject(json)
        val settingsJson = root.getJSONObject("settings")
        val settings = AppSettings(settingsJson.getInt("batteryCount"), settingsJson.getDouble("minVoltage"), settingsJson.getDouble("maxVoltage"))
        val array = root.getJSONArray("batteries")
        val batteries = buildList {
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                add(Battery(
                    id = item.getLong("id"), number = item.getInt("number"),
                    type = runCatching { BatteryType.valueOf(item.getString("type")) }.getOrDefault(BatteryType.BLUE),
                    voltage = item.getDouble("voltage"), sortOrder = item.getInt("sortOrder"),
                    isActive = item.optBoolean("isActive"), isRemoved = item.optBoolean("isRemoved"),
                    lastUpdatedAt = item.getLong("lastUpdatedAt"), createdAt = item.getLong("createdAt"),
                ))
            }
        }
        return AppData(settings, batteries)
    }

    companion object { const val FILE_NAME = "battdeck.json" }
}
