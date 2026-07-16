package com.catemup.battdeck.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catemup.battdeck.domain.BatteryStatus
import com.catemup.battdeck.domain.BatteryType
import com.catemup.battdeck.ui.theme.*

fun statusColor(status: BatteryStatus) = when (status) { BatteryStatus.READY -> NeonGreen; BatteryStatus.WARNING -> WarningOrange; else -> DangerRed }

@Composable fun PixelButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, color: Color = NeonGreen) {
    Box(modifier.heightIn(min = 48.dp).border(1.dp, if (enabled) color else TextMuted).clickable(enabled = enabled, onClick = onClick).padding(horizontal = 14.dp, vertical = 12.dp), contentAlignment = Alignment.Center) {
        Text(text, color = if (enabled) color else TextMuted, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}

@Composable fun TypeMarker(type: BatteryType, modifier: Modifier = Modifier) {
    Box(modifier.size(22.dp).background(if (type == BatteryType.BLUE) BatteryBlue else BatteryBlack).border(1.dp, if (type == BatteryType.BLUE) BatteryBlue else TextMuted))
}

@Composable fun BatteryBar(percent: Int, color: Color, modifier: Modifier = Modifier) {
    Box(modifier.height(8.dp).border(1.dp, Grid)) { Box(Modifier.fillMaxHeight().fillMaxWidth(percent / 100f).background(color)) }
}

@Composable fun VerticalBattery(percent: Int, color: Color, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.width(42.dp).height(10.dp).background(color))
        Box(Modifier.width(150.dp).height(260.dp).border(3.dp, color).padding(8.dp), contentAlignment = Alignment.BottomCenter) {
            Box(Modifier.fillMaxWidth().fillMaxHeight(percent / 100f).background(color.copy(alpha = .8f)))
        }
    }
}

@Composable fun Stepper(label: String, value: String, steps: List<Pair<String, () -> Unit>>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, color = TextMuted, fontSize = 13.sp)
        Text(value, color = TextPrimary, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) { steps.forEach { (text, action) -> PixelButton(text, action, Modifier.weight(1f), color = WarningOrange) } }
    }
}
