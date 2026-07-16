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

@Composable fun BatteryDetailsScreen(battery: Battery, settings: AppSettings, onCharge: () -> Unit, onRemove: (Battery) -> Unit, onCancel: () -> Unit, onSave: (Battery) -> Unit) {
    var draft by remember(battery.id) { mutableStateOf(battery) }
    val percent = BatteryRules.percent(draft.voltage, settings.minVoltage, settings.maxVoltage)
    val color = statusColor(BatteryRules.status(percent))
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("БАТАРЕЯ", color = NeonGreen, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text("НОМЕР", color = TextMuted); Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) { PixelButton("−", { draft = draft.copy(number = (draft.number - 1).coerceAtLeast(1)) }, Modifier.width(56.dp)); Text("%02d".format(draft.number), color = TextPrimary, fontSize = 42.sp, fontWeight = FontWeight.Bold); PixelButton("+", { draft = draft.copy(number = (draft.number + 1).coerceAtMost(99)) }, Modifier.width(56.dp)) }
        Text("ТИП КОМПЛЕКТУ", color = TextMuted); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) { PixelButton("СИНЯ", { draft = draft.copy(type = BatteryType.BLUE) }, Modifier.weight(1f), color = if (draft.type == BatteryType.BLUE) BatteryBlue else TextMuted); PixelButton("ЧОРНА", { draft = draft.copy(type = BatteryType.BLACK) }, Modifier.weight(1f), color = if (draft.type == BatteryType.BLACK) TextPrimary else TextMuted) }
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) { Text(String.format(Locale.US, "%.1f V", draft.voltage), color = color, fontSize = 34.sp); Text("$percent%", color = color, fontSize = 48.sp, fontWeight = FontWeight.Bold); BatteryBar(percent, color, Modifier.fillMaxWidth()); Spacer(Modifier.height(10.dp)); PixelButton("ЗМІНИТИ ЗАРЯД", onCharge, Modifier.fillMaxWidth(), color = color) }
        Text("ОНОВЛЕНО: ${BatteryRules.lastUpdatedText(false, draft.lastUpdatedAt)}", color = TextMuted)
        Text(if (draft.isActive) "СТАН: АКТИВНА" else "СТАН: НЕАКТИВНА", color = if (draft.isActive) NeonGreen else TextMuted)
        Spacer(Modifier.weight(1f)); PixelButton("ВИЛУЧИТИ", { onRemove(draft) }, Modifier.fillMaxWidth(), color = DangerRed)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { PixelButton("СКАСУВАТИ", onCancel, Modifier.weight(1f), color = TextMuted); PixelButton("ЗБЕРЕГТИ", { onSave(draft) }, Modifier.weight(1f), enabled = draft != battery) }
    }
}
