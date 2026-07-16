package com.catemup.battdeck.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catemup.battdeck.domain.*
import com.catemup.battdeck.ui.components.*
import com.catemup.battdeck.ui.theme.*
import java.util.Locale
import kotlin.math.round

@Composable fun ChargeEditScreen(battery: Battery, settings: AppSettings, onDetails: () -> Unit, onCancel: () -> Unit, onSave: (Battery) -> Unit) {
    var voltage by remember(battery.id) { mutableDoubleStateOf(battery.voltage.coerceIn(settings.minVoltage, settings.maxVoltage)) }
    fun change(delta: Double) { voltage = (round((voltage + delta) * 10) / 10).coerceIn(settings.minVoltage, settings.maxVoltage) }
    val percent = BatteryRules.percent(voltage, settings.minVoltage, settings.maxVoltage)
    val color = statusColor(BatteryRules.status(percent))
    Column(Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { TypeMarker(battery.type); Spacer(Modifier.width(10.dp)); Text("БАТАРЕЯ %02d".format(battery.number), color = TextPrimary, fontSize = 23.sp, fontWeight = FontWeight.Bold) }
        Text("$percent%", color = color, fontSize = 52.sp, fontWeight = FontWeight.Bold)
        Text(String.format(Locale.US, "%.1f V", voltage), color = color, fontSize = 28.sp)
        VerticalBattery(percent, color, Modifier.weight(1f, fill = false))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) { listOf(-1.0, -.1, .1, 1.0).forEach { step -> PixelButton(if (step > 0) "+$step" else step.toString(), { change(step) }, Modifier.weight(1f), color = WarningOrange) } }
        PixelButton("ДЕТАЛІ", onDetails, Modifier.fillMaxWidth(), color = BatteryBlue)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { PixelButton("СКАСУВАТИ", onCancel, Modifier.weight(1f), color = TextMuted); PixelButton("ЗБЕРЕГТИ", { onSave(battery.copy(voltage = voltage, lastUpdatedAt = System.currentTimeMillis())) }, Modifier.weight(1f)) }
    }
}
