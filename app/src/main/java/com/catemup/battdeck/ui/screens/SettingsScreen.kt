package com.catemup.battdeck.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.catemup.battdeck.domain.AppSettings
import com.catemup.battdeck.domain.AppLanguage
import com.catemup.battdeck.domain.BatteryMarking
import com.catemup.battdeck.domain.InputRules
import com.catemup.battdeck.ui.components.*
import com.catemup.battdeck.ui.inputErrorText
import com.catemup.battdeck.R
import com.catemup.battdeck.ui.theme.ReadyGreen
import com.catemup.battdeck.ui.theme.TextMuted
import com.catemup.battdeck.ui.theme.TextPrimary
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    initial: AppSettings,
    usedMarkingIndices: Set<Int>,
    selectedLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onCancel: () -> Unit,
    onSave: (AppSettings, List<Int>) -> Unit,
) {
    var countText by remember(initial) { mutableStateOf(initial.batteryCount.toString()) }
    var minText by remember(initial) { mutableStateOf(String.format(Locale.US, "%.1f", initial.minVoltage)) }
    var maxText by remember(initial) { mutableStateOf(String.format(Locale.US, "%.1f", initial.maxVoltage)) }
    var markings by remember(initial) { mutableStateOf(initial.markings) }
    var originalMarkingIndices by remember(initial) { mutableStateOf(initial.markings.indices.toList()) }
    val count = InputRules.integerOrNull(countText)
    val min = InputRules.decimalOrNull(minText)
    val max = InputRules.decimalOrNull(maxText)
    val countError = inputErrorText(InputRules.batteryCountError(countText))
    val minError = inputErrorText(InputRules.minVoltageError(minText, maxText))
    val maxError = inputErrorText(InputRules.maxVoltageError(maxText, minText))
    val markingsValid = markings.isNotEmpty() && markings.all { it.name.trim().isNotEmpty() && it.name.trim().length <= 24 }
    val valid = countError == null && minError == null && maxError == null && markingsValid
    val changed = count != initial.batteryCount || min != initial.minVoltage || max != initial.maxVoltage || selectedLanguage != initial.language || markings != initial.markings
    BackHandler(onBack = onCancel)

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.settings)) }, actions = { IconButton(onClick = onCancel) { Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close)) } }) },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.cancel)) }
                    Button(
                        onClick = { if (count != null && min != null && max != null) onSave(AppSettings(count, min, max, selectedLanguage, markings.map { it.copy(name = it.name.trim()) }), originalMarkingIndices) },
                        enabled = valid && changed, modifier = Modifier.weight(1f),
                    ) { Text(stringResource(R.string.save)) }
                }
            }
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SettingsSectionCard {
                Text(stringResource(R.string.language), style = MaterialTheme.typography.titleMedium)
                val options = listOf(AppLanguage.UKRAINIAN, AppLanguage.ENGLISH)
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    options.forEachIndexed { index, option ->
                        val ukrainian = option == AppLanguage.UKRAINIAN
                        SegmentedButton(
                            selected = selectedLanguage == option,
                            onClick = { onLanguageChange(option) },
                            shape = SegmentedButtonDefaults.itemShape(index, options.size),
                            modifier = Modifier.weight(1f),
                            label = { Text(stringResource(if (ukrainian) R.string.language_ukrainian else R.string.language_english)) },
                        )
                    }
                }
            }
            SettingsSectionCard {
                Text(stringResource(R.string.packs), style = MaterialTheme.typography.titleMedium)
                NumberTextField(countText, { countText = it }, stringResource(R.string.pack_count), countError, Modifier.fillMaxWidth())
            }
            SettingsSectionCard {
                Text(stringResource(R.string.voltage_scale), style = MaterialTheme.typography.titleMedium)
                VoltageTextField(minText, { minText = it }, stringResource(R.string.min_voltage), minError, Modifier.fillMaxWidth())
                VoltageTextField(maxText, { maxText = it }, stringResource(R.string.max_voltage), maxError, Modifier.fillMaxWidth())
                Text(stringResource(R.string.charge_scale), color = TextMuted)
                BatteryChargeBar(100, ReadyGreen)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.scale_min, minText.ifBlank { "—" }), color = TextMuted)
                    Text(stringResource(R.string.scale_max, maxText.ifBlank { "—" }), color = TextMuted)
                }
            }
            SettingsSectionCard {
                Text(stringResource(R.string.markings), style = MaterialTheme.typography.titleMedium)
                val palette = listOf(0xFF2F80EDL, 0xFF252C38L, 0xFF42D77DL, 0xFFFFA726L, 0xFFFF5C68L, 0xFF8E5BE8L, 0xFFFFD500L)
                markings.forEachIndexed { markingIndex, marking ->
                    val nameInvalid = marking.name.trim().isEmpty() || marking.name.trim().length > 24
                    OutlinedTextField(
                        value = marking.name,
                        onValueChange = { value -> if (value.length <= 24) markings = markings.mapIndexed { index, item -> if (index == markingIndex) item.copy(name = value) else item } },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.marking_name)) },
                        singleLine = true,
                        isError = nameInvalid,
                        supportingText = { if (nameInvalid) Text(stringResource(R.string.error_marking_name)) },
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        palette.chunked(4).forEach { colors ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                colors.forEach { colorValue ->
                                    Box(
                                        Modifier.size(34.dp).background(Color(colorValue), CircleShape)
                                            .then(if (marking.color == colorValue) Modifier.border(3.dp, TextPrimary, CircleShape) else Modifier)
                                            .clickable { markings = markings.mapIndexed { index, item -> if (index == markingIndex) item.copy(color = colorValue) else item } },
                                    )
                                }
                            }
                        }
                    }
                    val originalIndex = originalMarkingIndices[markingIndex]
                    val used = originalIndex in usedMarkingIndices
                    if (used) Text(stringResource(R.string.marking_in_use), color = TextMuted, style = MaterialTheme.typography.labelSmall)
                    TextButton(
                        onClick = {
                            markings = markings.filterIndexed { index, _ -> index != markingIndex }
                            originalMarkingIndices = originalMarkingIndices.filterIndexed { index, _ -> index != markingIndex }
                        },
                        enabled = !used && markings.size > 1,
                    ) { Text(stringResource(R.string.delete_marking)) }
                    HorizontalDivider()
                }
                OutlinedButton(
                    onClick = {
                        markings = markings + BatteryMarking("", 0xFF2F80EDL)
                        originalMarkingIndices = originalMarkingIndices + -1
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(R.string.add_marking)) }
            }
            SettingsSectionCard {
                Text(stringResource(R.string.data), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.export_json_description), color = TextMuted)
                OutlinedButton(onClick = onExport, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.export_json)) }
                Text(stringResource(R.string.import_json_description), color = TextMuted)
                OutlinedButton(onClick = onImport, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.import_json)) }
            }
        }
    }
}
