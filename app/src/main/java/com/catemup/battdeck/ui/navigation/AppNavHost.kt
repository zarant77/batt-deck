package com.catemup.battdeck.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.catemup.battdeck.domain.Battery
import com.catemup.battdeck.ui.screens.*
import com.catemup.battdeck.viewmodel.AppViewModel

private object Routes { const val LIST = "list"; const val SETTINGS = "settings"; const val HELP = "help"; const val DETAILS = "details/{id}"; const val CHARGE = "charge/{id}"; fun details(id: Long) = "details/$id"; fun charge(id: Long) = "charge/$id" }

@Composable fun AppNavHost(viewModel: AppViewModel) {
    val nav = rememberNavController()
    val state by viewModel.state.collectAsStateWithLifecycle()
    NavHost(nav, Routes.LIST) {
        composable(Routes.LIST) { BatteryListScreen(state.batteries, state.settings, { nav.navigate(Routes.details(it)) }, { nav.navigate(Routes.charge(it)) }, viewModel::activate, viewModel::reset, { nav.navigate(Routes.SETTINGS) }, { nav.navigate(Routes.HELP) }) }
        composable(Routes.HELP) { HelpScreen { nav.popBackStack() } }
        composable(Routes.SETTINGS) { SettingsScreen(state.settings, { nav.popBackStack() }) { viewModel.saveSettings(it) { nav.popBackStack() } } }
        composable(Routes.DETAILS, arguments = listOf(navArgument("id") { type = NavType.LongType })) { entry ->
            BatteryDestination(viewModel, entry.arguments?.getLong("id") ?: 0) { battery -> BatteryDetailsScreen(battery, state.settings, { nav.navigate(Routes.charge(battery.id)) }, { viewModel.remove(it) { nav.popBackStack(Routes.LIST, false) } }, { nav.popBackStack() }, { viewModel.saveBattery(it) { nav.popBackStack() } }) }
        }
        composable(Routes.CHARGE, arguments = listOf(navArgument("id") { type = NavType.LongType })) { entry ->
            BatteryDestination(viewModel, entry.arguments?.getLong("id") ?: 0) { battery -> ChargeEditScreen(battery, state.settings, { nav.navigate(Routes.details(battery.id)) }, { nav.popBackStack() }, { viewModel.saveBattery(it) { nav.popBackStack() } }) }
        }
    }
}

@Composable private fun BatteryDestination(viewModel: AppViewModel, id: Long, content: @Composable (Battery) -> Unit) {
    val battery by viewModel.battery(id).collectAsStateWithLifecycle(initialValue = null)
    battery?.let(content) ?: Text("БАТАРЕЮ НЕ ЗНАЙДЕНО", color = Color.White)
}
