package com.catemup.battdeck.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.catemup.battdeck.R
import com.catemup.battdeck.domain.Battery
import com.catemup.battdeck.domain.AppSettings
import com.catemup.battdeck.domain.InputRules
import com.catemup.battdeck.ui.components.BatteryNameTextField
import com.catemup.battdeck.ui.inputErrorText
import com.catemup.battdeck.ui.theme.DangerRed
import com.catemup.battdeck.ui.theme.TextPrimary
import com.catemup.battdeck.ui.components.markingColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryDetailsScreen(
    battery: Battery,
    settings: AppSettings,
    onRemove: (Battery) -> Unit,
    onCancel: () -> Unit,
    onSave: (Battery) -> Unit,
) {
    var draft by remember(battery.id) { mutableStateOf(battery) }
    var nameText by remember(battery.id) { mutableStateOf(battery.name) }
    var dateText by remember(battery.id) { mutableStateOf(InputRules.formatDate(battery.lastUpdatedAt)) }
    val nameError = inputErrorText(InputRules.nameError(nameText))
    val dateError = inputErrorText(InputRules.dateError(dateText))
    val dateTimestamp = InputRules.dateTimestamp(dateText)
    val initialDate = InputRules.formatDate(battery.lastUpdatedAt)
    val hasChanges = draft.markingIndex != battery.markingIndex || InputRules.normalizedName(nameText) != battery.name || dateText != initialDate

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.battery_settings_title, battery.name)) },
                actions = { IconButton(onClick = onCancel) { Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close)) } },
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.cancel)) }
                    Button(
                        onClick = {
                            val savedTimestamp = if (dateText == initialDate) battery.lastUpdatedAt else dateTimestamp
                            savedTimestamp?.let { onSave(draft.copy(name = InputRules.normalizedName(nameText), lastUpdatedAt = it)) }
                        },
                        enabled = nameError == null && dateError == null && hasChanges,
                        modifier = Modifier.weight(1f),
                    ) { Text(stringResource(R.string.save)) }
                }
            }
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            BatteryNameTextField(nameText, { nameText = it }, nameError, Modifier.fillMaxWidth())
            OutlinedTextField(
                value = dateText,
                onValueChange = { dateText = InputRules.formatDateInput(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.updated_date)) },
                placeholder = { Text(stringResource(R.string.date_placeholder)) },
                singleLine = true,
                isError = dateError != null,
                supportingText = { if (dateError != null) Text(dateError) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            Text(stringResource(R.string.battery_type), style = MaterialTheme.typography.labelLarge)
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    settings.markings.forEachIndexed { markingIndex, marking ->
                    FilterChip(
                        selected = draft.markingIndex == markingIndex,
                        onClick = { draft = draft.copy(markingIndex = markingIndex) },
                        label = { Text(marking.name) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = markingColor(marking), selectedLabelColor = TextPrimary),
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            OutlinedButton(
                onClick = { onRemove(draft) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
            ) { Text(stringResource(R.string.remove)) }
        }
    }
}
