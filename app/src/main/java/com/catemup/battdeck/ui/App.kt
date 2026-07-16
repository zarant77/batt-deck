package com.catemup.battdeck.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.catemup.battdeck.ui.navigation.AppNavHost
import com.catemup.battdeck.ui.theme.BattDeckTheme
import com.catemup.battdeck.viewmodel.AppViewModel

@Composable fun BattDeckApp(vm: AppViewModel = viewModel()) { BattDeckTheme { AppNavHost(vm) } }
