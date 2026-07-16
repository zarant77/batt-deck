package com.catemup.battdeck.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catemup.battdeck.domain.*
import com.catemup.battdeck.R
import com.catemup.battdeck.ui.components.*
import com.catemup.battdeck.ui.theme.*
import java.util.Locale
import kotlin.math.round

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChargeEditScreen(battery: Battery, settings: AppSettings, onSettings: () -> Unit, onCancel: () -> Unit, onSave: (Battery) -> Unit) {
    var voltage by remember(battery.id) { mutableDoubleStateOf(battery.voltage.coerceIn(settings.minVoltage, settings.maxVoltage)) }
    val percent = BatteryRules.percent(voltage, settings.minVoltage, settings.maxVoltage)
    val color = statusColor(BatteryRules.status(percent))
    val marking = settings.markings.getOrElse(battery.markingIndex) { settings.markings.first() }
    val markerColor = markingColor(marking)

    Scaffold(
        topBar = { TopAppBar(title = { Text(battery.name) }, actions = { IconButton(onClick = onCancel) { Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close)) } }) },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onSettings, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.settings)) }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.cancel)) }
                        Button(
                            onClick = { onSave(battery.copy(voltage = round(voltage * 10) / 10, lastUpdatedAt = System.currentTimeMillis())) },
                            modifier = Modifier.weight(1f),
                        ) { Text(stringResource(R.string.save)) }
                    }
                }
            }
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.width(18.dp).height(42.dp)
                        .background(markerColor, MaterialTheme.shapes.extraSmall)
                        .border(1.dp, Grid, MaterialTheme.shapes.extraSmall),
                )
                Spacer(Modifier.width(14.dp))
                Text(stringResource(R.string.voltage_value, voltage), color = color, fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text(stringResource(R.string.percent_value, percent), color = color, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
            Box(
                Modifier.weight(1f).fillMaxWidth().pointerInput(settings.minVoltage, settings.maxVoltage) {
                    val range = settings.maxVoltage - settings.minVoltage
                    detectVerticalDragGestures { change, amount ->
                        change.consume()
                        voltage = (voltage - (amount / size.height) * range * 2.2).coerceIn(settings.minVoltage, settings.maxVoltage)
                    }
                },
                contentAlignment = Alignment.Center,
            ) {
                VerticalBatteryIndicator(percent, color, bodyWidth = 164, bodyHeight = 300)
                Column(Modifier.align(Alignment.CenterEnd), horizontalAlignment = Alignment.End) {
                    Text(stringResource(R.string.percent_value, 100), color = TextMuted, style = MaterialTheme.typography.labelSmall)
                    Spacer(Modifier.height(260.dp))
                    Text(stringResource(R.string.percent_value, 0), color = TextMuted, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
