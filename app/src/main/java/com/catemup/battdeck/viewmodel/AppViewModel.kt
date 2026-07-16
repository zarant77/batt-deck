package com.catemup.battdeck.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.catemup.battdeck.BattDeckApplication
import com.catemup.battdeck.domain.AppSettings
import com.catemup.battdeck.domain.AppLanguage
import com.catemup.battdeck.domain.Battery
import com.catemup.battdeck.domain.BatteryRules
import com.catemup.battdeck.data.backup.BackupExporter
import com.catemup.battdeck.data.backup.BackupImporter
import com.catemup.battdeck.data.backup.ImportPreview
import com.catemup.battdeck.data.backup.ImportResult
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AppUiState(val batteries: List<Battery> = emptyList(), val settings: AppSettings = AppSettings(), val isLoading: Boolean = true)

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as BattDeckApplication
    private val repository = app.repository
    private val mutableLanguagePreview = MutableStateFlow<AppLanguage?>(null)
    private val mutableImportPreview = MutableStateFlow<ImportPreview?>(null)
    val languagePreview = mutableLanguagePreview.asStateFlow()
    val importPreview = mutableImportPreview.asStateFlow()
    val state = repository.data.map { data ->
        val ordered = data.batteries.filterNot { it.isRemoved }.sortedWith(
            compareBy<Battery> {
                BatteryRules.percent(it.voltage, data.settings.minVoltage, data.settings.maxVoltage) < 95
            }.thenBy { it.sortOrder },
        )
        AppUiState(ordered, data.settings, ordered.isEmpty())
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppUiState())

    init { viewModelScope.launch { repository.initialize() } }
    fun battery(id: Long) = repository.data.map { data -> data.batteries.firstOrNull { it.id == id && !it.isRemoved } }
    fun activate(id: Long) { viewModelScope.launch { repository.setActive(id) } }
    fun moveReady(id: Long, direction: Int) { viewModelScope.launch { repository.moveReady(id, direction) } }
    fun reset(id: Long) { viewModelScope.launch { repository.reset(id) } }
    fun saveBattery(value: Battery, onDone: () -> Unit = {}) { viewModelScope.launch { repository.saveBattery(value); onDone() } }
    fun remove(value: Battery, onDone: () -> Unit = {}) { viewModelScope.launch { repository.remove(value.id); onDone() } }
    fun previewLanguage(value: AppLanguage) { mutableLanguagePreview.value = value }
    fun cancelLanguagePreview() { mutableLanguagePreview.value = null }
    fun saveSettings(value: AppSettings, originalMarkingIndices: List<Int>, onDone: () -> Unit = {}) { viewModelScope.launch { repository.saveSettings(value, originalMarkingIndices); mutableLanguagePreview.value = null; onDone() } }
    fun exportJson(): String = BackupExporter.export(repository.data.value)
    fun prepareImport(json: String, fileName: String?): ImportResult = BackupImporter.parse(json).also {
        mutableImportPreview.value = (it as? ImportResult.Success)?.preview?.copy(fileName = fileName)
    }
    fun cancelImport() { mutableImportPreview.value = null }
    fun applyImport(onDone: () -> Unit = {}) {
        val preview = mutableImportPreview.value ?: return
        viewModelScope.launch { repository.replaceAll(preview.data); mutableImportPreview.value = null; mutableLanguagePreview.value = null; onDone() }
    }
}
