package com.catemup.battdeck.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catemup.battdeck.domain.AppSettings
import com.catemup.battdeck.ui.components.*
import com.catemup.battdeck.ui.theme.*
import java.util.Locale
import kotlin.math.round

@Composable fun SettingsScreen(initial: AppSettings, onCancel: () -> Unit, onSave: (AppSettings) -> Unit) {
    var draft by remember(initial) { mutableStateOf(initial) }
    fun rounded(value: Double) = round(value * 10) / 10
    val valid = draft.batteryCount in 1..99 && draft.maxVoltage > draft.minVoltage
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Text("НАЛАШТУВАННЯ", color = NeonGreen, fontSize = 25.sp, fontWeight = FontWeight.Bold)
        Stepper("КІЛЬКІСТЬ КОМПЛЕКТІВ", draft.batteryCount.toString(), listOf("−" to { draft = draft.copy(batteryCount = (draft.batteryCount - 1).coerceAtLeast(1)) }, "+" to { draft = draft.copy(batteryCount = (draft.batteryCount + 1).coerceAtMost(99)) }))
        Stepper("МІНІМАЛЬНА НАПРУГА", String.format(Locale.US, "%.1f V", draft.minVoltage), listOf("−1.0" to { draft = draft.copy(minVoltage = rounded(draft.minVoltage - 1)) }, "−0.1" to { draft = draft.copy(minVoltage = rounded(draft.minVoltage - .1)) }, "+0.1" to { draft = draft.copy(minVoltage = rounded(draft.minVoltage + .1)) }, "+1.0" to { draft = draft.copy(minVoltage = rounded(draft.minVoltage + 1)) }))
        Stepper("МАКСИМАЛЬНА НАПРУГА", String.format(Locale.US, "%.1f V", draft.maxVoltage), listOf("−1.0" to { draft = draft.copy(maxVoltage = rounded(draft.maxVoltage - 1)) }, "−0.1" to { draft = draft.copy(maxVoltage = rounded(draft.maxVoltage - .1)) }, "+0.1" to { draft = draft.copy(maxVoltage = rounded(draft.maxVoltage + .1)) }, "+1.0" to { draft = draft.copy(maxVoltage = rounded(draft.maxVoltage + 1)) }))
        Text("ШКАЛА ЗАРЯДУ", color = TextMuted); BatteryBar(100, NeonGreen, Modifier.fillMaxWidth()); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(String.format(Locale.US, "%.1f V · 0%%", draft.minVoltage), color = DangerRed); Text(String.format(Locale.US, "%.1f V · 100%%", draft.maxVoltage), color = NeonGreen) }
        if (!valid) Text("МАКСИМУМ МАЄ БУТИ БІЛЬШИМ ЗА МІНІМУМ", color = DangerRed)
        Spacer(Modifier.weight(1f)); Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { PixelButton("СКАСУВАТИ", onCancel, Modifier.weight(1f), color = TextMuted); PixelButton("ЗБЕРЕГТИ", { onSave(draft) }, Modifier.weight(1f), enabled = valid && draft != initial) }
    }
}
