package com.catemup.battdeck.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.catemup.battdeck.R
import com.catemup.battdeck.domain.InputError
import com.catemup.battdeck.domain.InputRules

@Composable
fun updatedText(days: Int?): String = when (days) {
    null -> stringResource(R.string.updated_never)
    0 -> stringResource(R.string.updated_today)
    1 -> stringResource(R.string.updated_yesterday)
    else -> pluralStringResource(R.plurals.updated_days_ago, days, days)
}

@Composable
fun inputErrorText(error: InputError?, min: Double = 0.0, max: Double = 0.0): String? = when (error) {
    null -> null
    InputError.NAME_REQUIRED -> stringResource(R.string.error_name_required)
    InputError.NAME_TOO_LONG -> stringResource(R.string.error_name_too_long, InputRules.MAX_NAME_LENGTH)
    InputError.VOLTAGE_REQUIRED -> stringResource(R.string.error_voltage_required)
    InputError.INVALID_NUMBER -> stringResource(R.string.error_invalid_number)
    InputError.VOLTAGE_OUT_OF_RANGE -> stringResource(R.string.error_voltage_range, min, max)
    InputError.INTEGER_REQUIRED -> stringResource(R.string.error_integer_required)
    InputError.COUNT_OUT_OF_RANGE -> stringResource(R.string.error_count_range, InputRules.MIN_BATTERY_COUNT, InputRules.MAX_BATTERY_COUNT)
    InputError.MIN_NOT_LOWER -> stringResource(R.string.error_min_not_lower)
    InputError.MAX_NOT_HIGHER -> stringResource(R.string.error_max_not_higher)
    InputError.DATE_REQUIRED -> stringResource(R.string.error_date_required)
    InputError.INVALID_DATE -> stringResource(R.string.error_invalid_date)
    InputError.DATE_IN_FUTURE -> stringResource(R.string.error_date_in_future)
}
