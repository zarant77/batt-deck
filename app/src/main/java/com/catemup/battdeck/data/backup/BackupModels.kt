package com.catemup.battdeck.data.backup

import com.catemup.battdeck.data.AppData

data class ImportPreview(
    val data: AppData,
    val fileName: String? = null,
    val batteryCount: Int,
    val exportedAt: Long?,
    val hasSettings: Boolean,
)

sealed interface ImportResult {
    data class Success(val preview: ImportPreview) : ImportResult
    data object NotBattDeck : ImportResult
    data object UnsupportedVersion : ImportResult
    data object InvalidJson : ImportResult
    data object InvalidBatteries : ImportResult
}
