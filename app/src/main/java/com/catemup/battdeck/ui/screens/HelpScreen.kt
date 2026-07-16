package com.catemup.battdeck.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catemup.battdeck.ui.components.PixelButton
import com.catemup.battdeck.ui.theme.*

@Composable fun HelpScreen(onClose: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text("ДОПОМОГА", color = NeonGreen, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text("BATTDECK ДОПОМАГАЄ ВЕСТИ ОФЛАЙН-ОБЛІК КОМПЛЕКТІВ ТА ЇХ ЧЕРГИ.", color = TextPrimary)
        Text("КОЛЬОРИ ЗАРЯДУ\n≥95%  ЗЕЛЕНИЙ\n≥50%  ПОМАРАНЧЕВИЙ\n<50%   ЧЕРВОНИЙ", color = TextPrimary, lineHeight = 27.sp)
        Text("ТАП ПО ЗАРЯДУ — ЗМІНИТИ ЗАРЯД.\n\nСВАЙП ВПРАВО — АКТИВУВАТИ.\n\nСВАЙП ВЛІВО — СКИНУТИ.\n\nПЕРЕТЯГУВАННЯ ДЛЯ ЗМІНИ ЧЕРГИ — У НАСТУПНІЙ ВЕРСІЇ.\n\nУ НАЛАШТУВАННЯХ ЗМІНЮЄТЬСЯ КІЛЬКІСТЬ ТА ШКАЛА НАПРУГИ.", color = TextMuted, lineHeight = 22.sp)
        Spacer(Modifier.weight(1f)); PixelButton("ЗАКРИТИ", onClose, Modifier.fillMaxWidth())
    }
}
