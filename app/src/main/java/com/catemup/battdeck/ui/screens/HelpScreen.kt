package com.catemup.battdeck.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.catemup.battdeck.ui.theme.TextMuted
import com.catemup.battdeck.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(onClose: () -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.help)) }, actions = { IconButton(onClick = onClose) { Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close)) } }) },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(stringResource(R.string.help_intro), style = MaterialTheme.typography.bodyLarge)
            HelpCard(stringResource(R.string.help_colors_title), stringResource(R.string.help_colors_text))
            HelpCard(stringResource(R.string.help_controls_title), stringResource(R.string.help_controls_text))
            HelpCard(stringResource(R.string.help_gestures_title), stringResource(R.string.help_gestures_text))
            HelpCard(stringResource(R.string.help_settings_title), stringResource(R.string.help_settings_text))
            Spacer(Modifier.weight(1f))
            Button(onClick = onClose, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.close)) }
        }
    }
}

@Composable
private fun HelpCard(title: String, text: String) {
    OutlinedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(text, color = TextMuted)
        }
    }
}
