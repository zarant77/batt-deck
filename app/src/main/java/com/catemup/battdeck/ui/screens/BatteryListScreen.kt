package com.catemup.battdeck.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catemup.battdeck.domain.*
import com.catemup.battdeck.ui.components.*
import com.catemup.battdeck.ui.theme.*
import java.util.Locale

@Composable fun BatteryListScreen(
    batteries: List<Battery>, settings: AppSettings,
    onDetails: (Long) -> Unit, onCharge: (Long) -> Unit, onActivate: (Long) -> Unit, onReset: (Long) -> Unit,
    onSettings: () -> Unit, onHelp: () -> Unit,
) {
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("BATTDECK", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = NeonGreen, modifier = Modifier.weight(1f))
            Text("v0.1.0", color = TextMuted, fontSize = 11.sp)
            Spacer(Modifier.width(10.dp)); PixelButton("?", onHelp, Modifier.width(48.dp))
        }
        Spacer(Modifier.height(12.dp))
        if (batteries.isEmpty()) Text("ЗАВАНТАЖЕННЯ…", color = TextMuted)
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(9.dp), contentPadding = PaddingValues(bottom = 12.dp)) {
            items(batteries, key = { it.id }) { battery ->
                BatteryCard(battery, settings, { onDetails(battery.id) }, { onCharge(battery.id) }, { onActivate(battery.id) }, { onReset(battery.id) })
            }
        }
        PixelButton("НАЛАШТУВАННЯ", onSettings, Modifier.fillMaxWidth())
    }
}

@Composable private fun BatteryCard(battery: Battery, settings: AppSettings, onDetails: () -> Unit, onCharge: () -> Unit, onActivate: () -> Unit, onReset: () -> Unit) {
    val percent = BatteryRules.percent(battery.voltage, settings.minVoltage, settings.maxVoltage)
    val color = statusColor(BatteryRules.status(percent))
    var drag = 0f
    Column(Modifier.fillMaxWidth().border(if (battery.isActive) 2.dp else 1.dp, if (battery.isActive) NeonGreen else Grid)
        .pointerInput(battery.id) { detectHorizontalDragGestures(onDragEnd = { if (drag > 80) onActivate() else if (drag < -80) onReset(); drag = 0f }) { _, amount -> drag += amount } }
        .clickable(onClick = onDetails).padding(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TypeMarker(battery.type); Spacer(Modifier.width(10.dp))
            Text("%02d".format(battery.number), fontSize = 25.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(10.dp)); Text(BatteryRules.lastUpdatedText(battery.isActive, battery.lastUpdatedAt), Modifier.weight(1f), color = if (battery.isActive) NeonGreen else TextMuted, fontSize = 12.sp)
            Row(Modifier.clickable(onClick = onCharge).padding(6.dp), verticalAlignment = Alignment.Bottom) {
                Text(String.format(Locale.US, "%.1fv", battery.voltage), color = color, fontSize = 17.sp); Spacer(Modifier.width(8.dp)); Text("$percent%", color = color, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(8.dp)); BatteryBar(percent, color, Modifier.fillMaxWidth().clickable(onClick = onCharge))
    }
}
