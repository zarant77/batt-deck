package com.catemup.battdeck.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catemup.battdeck.R
import com.catemup.battdeck.BuildConfig
import com.catemup.battdeck.ui.updatedText
import com.catemup.battdeck.domain.*
import com.catemup.battdeck.ui.components.BatteryChargeBar
import com.catemup.battdeck.ui.components.statusColor
import com.catemup.battdeck.ui.components.markingColor
import com.catemup.battdeck.ui.theme.*
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryListScreen(
    batteries: List<Battery>, settings: AppSettings,
    onDetails: (Long) -> Unit, onCharge: (Long) -> Unit, onToggleActive: (Long) -> Unit, onMoveReady: (Long, Int) -> Unit,
    onSettings: () -> Unit, onHelp: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(painterResource(R.mipmap.ic_launcher_foreground), contentDescription = null, modifier = Modifier.size(52.dp))
                        Spacer(Modifier.width(10.dp))
                        Column {
                            val appName = stringResource(R.string.app_name)
                            Text(
                                buildAnnotatedString {
                                    withStyle(SpanStyle(color = UkrainianBlue, fontWeight = FontWeight.Bold)) { append(appName.take(4)) }
                                    withStyle(SpanStyle(color = UkrainianYellow, fontWeight = FontWeight.Bold)) { append(appName.drop(4)) }
                                    append(" ")
                                    withStyle(SpanStyle(color = TextMuted, fontSize = 14.sp, fontWeight = FontWeight.Normal)) {
                                        append("v${BuildConfig.VERSION_NAME}")
                                    }
                                },
                            )
                            Text(stringResource(R.string.app_subtitle), style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onHelp) { Icon(Icons.Default.Info, contentDescription = stringResource(R.string.help)) }
                    IconButton(onClick = onSettings) { Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings)) }
                },
            )
        },
    ) { padding ->
        if (batteries.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                val readyIds = batteries.filter {
                    BatteryRules.percent(it.voltage, settings.minVoltage, settings.maxVoltage) >= 95
                }.map { it.id }
                items(batteries, key = { it.id }) { battery ->
                    val readyIndex = readyIds.indexOf(battery.id)
                    BatteryCard(
                        modifier = Modifier.animateItem(),
                        battery = battery,
                        settings = settings,
                        onDetails = { onDetails(battery.id) },
                        onCharge = { onCharge(battery.id) },
                        onToggleActive = { onToggleActive(battery.id) },
                        canMoveUp = readyIndex > 0,
                        canMoveDown = readyIndex >= 0 && readyIndex < readyIds.lastIndex,
                        onMove = { direction -> onMoveReady(battery.id, direction) },
                    )
                }
            }
        }
    }
}

@Composable
private fun BatteryCard(
    modifier: Modifier = Modifier,
    battery: Battery, settings: AppSettings,
    onDetails: () -> Unit, onCharge: () -> Unit, onToggleActive: () -> Unit,
    canMoveUp: Boolean, canMoveDown: Boolean, onMove: (Int) -> Unit,
) {
    val percent = BatteryRules.percent(battery.voltage, settings.minVoltage, settings.maxVoltage)
    val color = statusColor(BatteryRules.status(percent))
    val marking = settings.markings.getOrElse(battery.markingIndex) { settings.markings.first() }
    val swipeOffset = remember(battery.id) { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val shape = MaterialTheme.shapes.medium

    Box(
        modifier.fillMaxWidth().clip(shape)
            .background(if (battery.isActive) DangerRed.copy(alpha = .75f) else ReadyGreen.copy(alpha = .7f)),
        contentAlignment = if (swipeOffset.value >= 0) Alignment.CenterStart else Alignment.CenterEnd,
    ) {
        Text(
            stringResource(if (battery.isActive) R.string.deactivate else R.string.activate),
            modifier = Modifier.padding(horizontal = 18.dp),
            color = Background,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelMedium,
        )
        OutlinedCard(
            modifier = Modifier.fillMaxWidth()
                .graphicsLayer { translationX = swipeOffset.value }
                .pointerInput(battery.id, battery.isActive) {
                    val threshold = size.width * .22f
                    detectHorizontalDragGestures(
                        onDragCancel = { scope.launch { swipeOffset.animateTo(0f, tween(160)) } },
                        onDragEnd = {
                            if (abs(swipeOffset.value) >= threshold) onToggleActive()
                            scope.launch { swipeOffset.animateTo(0f, tween(180)) }
                        },
                    ) { change, amount ->
                        change.consume()
                        scope.launch { swipeOffset.snapTo((swipeOffset.value + amount).coerceIn(-size.width * .7f, size.width * .7f)) }
                    }
                }
                .clickable(onClick = onDetails),
            colors = CardDefaults.outlinedCardColors(containerColor = if (battery.isActive) PanelActive else Panel),
            border = if (battery.isActive) BorderStroke(2.dp, UkrainianBlue) else BorderStroke(1.dp, Grid),
        ) {
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                BatteryDragHandle(
                    marking = marking,
                    enabled = percent >= 95,
                    canMoveUp = canMoveUp,
                    canMoveDown = canMoveDown,
                    onMove = onMove,
                )
                Column(
                    Modifier.weight(1f).padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            battery.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        Text(stringResource(R.string.voltage_value, battery.voltage), color = color, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.width(10.dp))
                        Text(stringResource(R.string.percent_value, percent), color = color, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            updatedText(BatteryRules.daysSinceUpdate(battery.lastUpdatedAt)),
                            style = MaterialTheme.typography.labelMedium,
                            color = TextMuted,
                            modifier = Modifier.weight(1f),
                        )
                        BatteryChargeBar(percent, color, Modifier.width(118.dp).clickable(onClick = onCharge), height = 4)
                    }
                }
            }
        }
    }
}

@Composable
private fun BatteryDragHandle(
    marking: BatteryMarking, enabled: Boolean, canMoveUp: Boolean, canMoveDown: Boolean, onMove: (Int) -> Unit,
) {
    var distance by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    val currentCanMoveUp by rememberUpdatedState(canMoveUp)
    val currentCanMoveDown by rememberUpdatedState(canMoveDown)
    val currentOnMove by rememberUpdatedState(onMove)
    val baseColor = markingColor(marking)
    val handleColor = if (isDragging) baseColor.copy(alpha = .65f) else baseColor
    Box(
        Modifier.width(44.dp).fillMaxHeight()
            .background(handleColor)
            .border(1.dp, Grid)
            .pointerInput(enabled) {
                if (enabled) {
                    val reorderThreshold = 36.dp.toPx()
                    detectVerticalDragGestures(
                        onDragStart = { distance = 0f; isDragging = true },
                        onDragCancel = { distance = 0f; isDragging = false },
                        onDragEnd = { distance = 0f; isDragging = false },
                    ) { change, amount ->
                        change.consume()
                        distance += amount
                        if (distance <= -reorderThreshold && currentCanMoveUp) {
                            currentOnMove(-1)
                            distance = 0f
                        } else if (distance >= reorderThreshold && currentCanMoveDown) {
                            currentOnMove(1)
                            distance = 0f
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        if (enabled) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                repeat(3) { Box(Modifier.width(18.dp).height(2.dp).background(TextPrimary.copy(alpha = if (isDragging) 1f else .75f))) }
            }
        }
    }
}
