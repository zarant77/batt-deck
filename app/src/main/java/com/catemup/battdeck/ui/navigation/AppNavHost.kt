package com.catemup.battdeck.ui.navigation

import android.content.Intent
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.catemup.battdeck.domain.Battery
import com.catemup.battdeck.domain.AppLanguage
import com.catemup.battdeck.R
import com.catemup.battdeck.ui.screens.*
import com.catemup.battdeck.viewmodel.AppViewModel
import com.catemup.battdeck.data.backup.ImportResult
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private object Routes { const val LIST = "list"; const val SETTINGS = "settings"; const val HELP = "help"; const val DETAILS = "details/{id}"; const val CHARGE = "charge/{id}"; fun details(id: Long) = "details/$id"; fun charge(id: Long) = "charge/$id" }

@Composable fun AppNavHost(viewModel: AppViewModel) {
    val context = LocalContext.current
    val nav = rememberNavController()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val languagePreview by viewModel.languagePreview.collectAsStateWithLifecycle()
    val importPreview by viewModel.importPreview.collectAsStateWithLifecycle()
    val importPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) {
            Toast.makeText(context, context.getString(R.string.import_cancelled), Toast.LENGTH_SHORT).show()
        } else {
            val fileName = runCatching {
                context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) cursor.getString(0) else null
                }
            }.getOrNull()
            val result = runCatching { context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } }
            val parsed = result.getOrNull()?.let { viewModel.prepareImport(it, fileName) } ?: ImportResult.InvalidJson
            val error = when (parsed) {
                ImportResult.NotBattDeck -> R.string.file_not_battdeck
                ImportResult.UnsupportedVersion -> R.string.unsupported_format
                ImportResult.InvalidJson -> R.string.cannot_read_json
                ImportResult.InvalidBatteries -> R.string.invalid_battery_data
                is ImportResult.Success -> null
            }
            if (error != null) Toast.makeText(context, context.getString(error), Toast.LENGTH_LONG).show()
        }
    }
    val effectiveLanguage = languagePreview ?: state.settings.language
    LaunchedEffect(state.isLoading, effectiveLanguage) {
        if (!state.isLoading) {
            val tags = when (effectiveLanguage) {
                AppLanguage.SYSTEM -> ""
                AppLanguage.UKRAINIAN -> "uk"
                AppLanguage.ENGLISH -> "en"
            }
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tags))
        }
    }
    NavHost(nav, Routes.LIST) {
        composable(Routes.LIST) { BatteryListScreen(state.batteries, state.settings, { nav.navigate(Routes.charge(it)) }, { nav.navigate(Routes.charge(it)) }, viewModel::activate, viewModel::moveReady, { nav.navigate(Routes.SETTINGS) }, { nav.navigate(Routes.HELP) }) }
        composable(Routes.HELP) { HelpScreen { nav.popBackStack() } }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                initial = state.settings,
                usedMarkingIndices = state.batteries.mapTo(mutableSetOf()) { it.markingIndex },
                selectedLanguage = effectiveLanguage,
                onLanguageChange = viewModel::previewLanguage,
                onExport = {
                    runCatching {
                        val directory = File(context.cacheDir, "exports").apply { mkdirs() }
                        val timestamp = SimpleDateFormat("yyyy-MM-dd-HH-mm", Locale.US).format(Date())
                        val file = File(directory, "battdeck-export-$timestamp.json").apply { writeText(viewModel.exportJson()) }
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                            type = "application/json"; putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }, null))
                    }.onFailure { Toast.makeText(context, context.getString(R.string.cannot_read_json), Toast.LENGTH_LONG).show() }
                },
                onImport = { importPicker.launch(arrayOf("application/json", "text/plain", "*/*")) },
                onCancel = { viewModel.cancelLanguagePreview(); nav.popBackStack() },
                onSave = { settings, originalIndices -> viewModel.saveSettings(settings, originalIndices) { nav.popBackStack() } },
            )
        }
        composable(Routes.DETAILS, arguments = listOf(navArgument("id") { type = NavType.LongType })) { entry ->
            BatteryDestination(viewModel, entry.arguments?.getLong("id") ?: 0) { battery -> BatteryDetailsScreen(battery, state.settings, { viewModel.remove(it) { nav.popBackStack(Routes.LIST, false) } }, { nav.popBackStack() }, { viewModel.saveBattery(it) { nav.popBackStack() } }) }
        }
        composable(Routes.CHARGE, arguments = listOf(navArgument("id") { type = NavType.LongType })) { entry ->
            BatteryDestination(viewModel, entry.arguments?.getLong("id") ?: 0) { battery ->
                ChargeEditScreen(
                    battery,
                    state.settings,
                    onSettings = {
                        if (nav.previousBackStackEntry?.destination?.route == Routes.DETAILS) nav.popBackStack()
                        else nav.navigate(Routes.details(battery.id))
                    },
                    onCancel = { nav.popBackStack() },
                    onSave = { viewModel.saveBattery(it) { nav.popBackStack() } },
                )
            }
        }
    }
    if (importPreview != null) {
        val preview = importPreview!!
        val exported = preview.exportedAt?.let { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(it)) }
        val batteryCountText = stringResource(R.string.import_battery_count, preview.batteryCount)
        val fileNameText = preview.fileName?.let { stringResource(R.string.import_file_name, it) }
        val exportedAtText = exported?.let { stringResource(R.string.import_exported_at, it) }
        val settingsFoundText = stringResource(
            R.string.import_settings_found,
            preview.data.settings.batteryCount,
            preview.data.settings.minVoltage,
            preview.data.settings.maxVoltage,
        )
        AlertDialog(
            onDismissRequest = viewModel::cancelImport,
            title = { Text(stringResource(R.string.import_preview)) },
            text = {
                Text(buildString {
                    if (fileNameText != null) append(fileNameText).append("\n")
                    append(batteryCountText)
                    if (exportedAtText != null) append("\n").append(exportedAtText)
                    if (preview.hasSettings) append("\n").append(settingsFoundText)
                })
            },
            confirmButton = { Button(onClick = { viewModel.applyImport { Toast.makeText(context, context.getString(R.string.import_completed), Toast.LENGTH_SHORT).show() } }) { Text(stringResource(R.string.import_action)) } },
            dismissButton = { OutlinedButton(onClick = viewModel::cancelImport) { Text(stringResource(R.string.cancel)) } },
        )
    }
}

@Composable private fun BatteryDestination(viewModel: AppViewModel, id: Long, content: @Composable (Battery) -> Unit) {
    val battery by viewModel.battery(id).collectAsStateWithLifecycle(initialValue = null)
    val currentBattery = battery
    if (currentBattery != null) {
        content(currentBattery)
    } else {
        Text(stringResource(R.string.battery_not_found), color = Color.White)
    }
}
