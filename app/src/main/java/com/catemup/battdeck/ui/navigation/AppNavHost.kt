package com.catemup.battdeck.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.catemup.battdeck.domain.Battery
import com.catemup.battdeck.domain.AppLanguage
import com.catemup.battdeck.R
import com.catemup.battdeck.ui.screens.*
import com.catemup.battdeck.viewmodel.AppViewModel

private object Routes { const val LIST = "list"; const val SETTINGS = "settings"; const val HELP = "help"; const val DETAILS = "details/{id}"; const val CHARGE = "charge/{id}"; fun details(id: Long) = "details/$id"; fun charge(id: Long) = "charge/$id" }

@Composable fun AppNavHost(viewModel: AppViewModel) {
    val nav = rememberNavController()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val languagePreview by viewModel.languagePreview.collectAsStateWithLifecycle()
    val effectiveLanguage = languagePreview ?: state.settings.language
    LaunchedEffect(state.isLoading, effectiveLanguage) {
        if (!state.isLoading) {
            val tags = when (effectiveLanguage) {
                AppLanguage.SYSTEM -> ""
                AppLanguage.UKRAINIAN -> "uk"
                AppLanguage.ENGLISH -> "en"
            }
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tags))
        }
    }
    NavHost(nav, Routes.LIST) {
        composable(Routes.LIST) { BatteryListScreen(state.batteries, state.settings, { nav.navigate(Routes.charge(it)) }, { nav.navigate(Routes.charge(it)) }, viewModel::activate, viewModel::moveReady, { nav.navigate(Routes.SETTINGS) }, { nav.navigate(Routes.HELP) }) }
        composable(Routes.HELP) { HelpScreen { nav.popBackStack() } }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                initial = state.settings,
                usedMarkingIds = state.batteries.mapTo(mutableSetOf()) { it.markingId },
                selectedLanguage = effectiveLanguage,
                onLanguageChange = viewModel::previewLanguage,
                onCancel = { viewModel.cancelLanguagePreview(); nav.popBackStack() },
                onSave = { viewModel.saveSettings(it) { nav.popBackStack() } },
            )
        }
        composable(Routes.DETAILS, arguments = listOf(navArgument("id") { type = NavType.LongType })) { entry ->
            BatteryDestination(viewModel, entry.arguments?.getLong("id") ?: 0) { battery -> BatteryDetailsScreen(battery, state.settings, { viewModel.remove(it) { nav.popBackStack(Routes.LIST, false) } }, { nav.popBackStack() }, { viewModel.saveBattery(it) { nav.popBackStack() } }) }
        }
        composable(Routes.CHARGE, arguments = listOf(navArgument("id") { type = NavType.LongType })) { entry ->
            BatteryDestination(viewModel, entry.arguments?.getLong("id") ?: 0) { battery ->
                ChargeEditScreen(
                    battery,
                    state.settings,
                    onSettings = {
                        if (nav.previousBackStackEntry?.destination?.route == Routes.DETAILS) nav.popBackStack()
                        else nav.navigate(Routes.details(battery.id))
                    },
                    onCancel = { nav.popBackStack() },
                    onSave = { viewModel.saveBattery(it) { nav.popBackStack() } },
                )
            }
        }
    }
}

@Composable private fun BatteryDestination(viewModel: AppViewModel, id: Long, content: @Composable (Battery) -> Unit) {
    val battery by viewModel.battery(id).collectAsStateWithLifecycle(initialValue = null)
    val currentBattery = battery
    if (currentBattery != null) {
        content(currentBattery)
    } else {
        Text(stringResource(R.string.battery_not_found), color = Color.White)
    }
}
