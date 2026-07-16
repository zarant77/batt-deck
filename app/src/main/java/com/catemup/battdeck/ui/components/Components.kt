package com.catemup.battdeck.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.catemup.battdeck.R
import com.catemup.battdeck.domain.BatteryStatus
import com.catemup.battdeck.domain.BatteryMarking
import com.catemup.battdeck.domain.InputRules
import com.catemup.battdeck.ui.theme.*

fun statusColor(status: BatteryStatus) = when (status) {
    BatteryStatus.READY -> ReadyGreen
    BatteryStatus.WARNING -> WarningOrange
    BatteryStatus.DANGER -> DangerRed
}

@Composable
fun BatteryChargeBar(percent: Int, color: Color, modifier: Modifier = Modifier, height: Int = 8) {
    LinearProgressIndicator(
        progress = { percent.coerceIn(0, 100) / 100f },
        modifier = modifier.fillMaxWidth().height(height.dp),
        color = color,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
    )
}

@Composable
fun VerticalBatteryIndicator(
    percent: Int,
    color: Color,
    modifier: Modifier = Modifier,
    bodyWidth: Int = 116,
    bodyHeight: Int = 190,
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.width((bodyWidth / 3).dp).height(10.dp).background(color, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)))
        Box(
            Modifier.width(bodyWidth.dp).height(bodyHeight.dp).border(3.dp, color, RoundedCornerShape(12.dp)).padding(8.dp),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Box(
                Modifier.fillMaxWidth().fillMaxHeight(percent.coerceIn(0, 100) / 100f)
                    .background(color.copy(alpha = .8f), RoundedCornerShape(5.dp)),
            )
        }
    }
}

@Composable
fun markingColor(marking: BatteryMarking) = Color(marking.color)

@Composable
fun BatteryMarkingChip(marking: BatteryMarking) {
    val color = markingColor(marking)
    AssistChip(
        onClick = {},
        enabled = false,
        label = { Text(marking.name) },
        colors = AssistChipDefaults.assistChipColors(
            disabledContainerColor = color.copy(alpha = .28f),
            disabledLabelColor = TextPrimary,
        ),
    )
}

@Composable
fun BatteryStatusChip() {
    AssistChip(
        onClick = {}, enabled = false, label = { Text(stringResource(R.string.active)) },
        colors = AssistChipDefaults.assistChipColors(
            disabledContainerColor = ReadyGreen.copy(alpha = .18f), disabledLabelColor = ReadyGreen,
        ),
    )
}

@Composable
fun BatteryNameTextField(value: String, onValueChange: (String) -> Unit, error: String?, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = { if (it.length <= InputRules.MAX_NAME_LENGTH) onValueChange(it) },
        modifier = modifier,
        label = { Text(stringResource(R.string.battery_name)) },
        placeholder = { Text(stringResource(R.string.battery_name_placeholder)) },
        singleLine = true,
        isError = error != null,
        supportingText = { Text(error ?: stringResource(R.string.name_hint, InputRules.MAX_NAME_LENGTH)) },
    )
}

@Composable
fun VoltageTextField(value: String, onValueChange: (String) -> Unit, label: String, error: String?, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = { if (InputRules.isDecimalDraft(it)) onValueChange(it) },
        modifier = modifier,
        label = { Text(label) },
        suffix = { Text(stringResource(R.string.voltage_suffix)) },
        singleLine = true,
        isError = error != null,
        supportingText = { if (error != null) Text(error) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
    )
}

@Composable
fun NumberTextField(value: String, onValueChange: (String) -> Unit, label: String, error: String?, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = { if (InputRules.isIntegerDraft(it)) onValueChange(it) },
        modifier = modifier,
        label = { Text(label) },
        singleLine = true,
        isError = error != null,
        supportingText = { if (error != null) Text(error) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
}

@Composable
fun SettingsSectionCard(content: @Composable ColumnScope.() -> Unit) {
    OutlinedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}
