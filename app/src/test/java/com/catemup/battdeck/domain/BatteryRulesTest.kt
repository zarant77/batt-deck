package com.catemup.battdeck.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class BatteryRulesTest {
    @Test fun percentageIsCalculatedAndClamped() {
        assertEquals(0, BatteryRules.percent(35.0, 40.2, 50.2))
        assertEquals(50, BatteryRules.percent(45.2, 40.2, 50.2))
        assertEquals(100, BatteryRules.percent(60.0, 40.2, 50.2))
    }
    @Test fun statusUsesDocumentedThresholds() {
        assertEquals(BatteryStatus.READY, BatteryRules.status(95))
        assertEquals(BatteryStatus.WARNING, BatteryRules.status(50))
        assertEquals(BatteryStatus.DANGER, BatteryRules.status(1))
        assertEquals(BatteryStatus.DANGER, BatteryRules.status(0))
    }

    @Test fun inputRulesAcceptUkrainianDecimalAndRejectInvalidValues() {
        assertEquals(40.2, InputRules.decimalOrNull("40,2")!!, 0.0)
        assertNull(InputRules.voltageError("45.5", 40.2, 50.2))
        assertEquals(InputError.NAME_REQUIRED, InputRules.nameError("   "))
        assertEquals(InputError.COUNT_OUT_OF_RANGE, InputRules.batteryCountError("51"))
    }

    @Test fun dateInputIsFormattedAndValidatedStrictly() {
        assertEquals("31.12.2025", InputRules.formatDateInput("31122025"))
        assertEquals(InputError.INVALID_DATE, InputRules.dateError("31.02.2025", LocalDate.of(2025, 12, 31)))
        assertEquals(InputError.DATE_IN_FUTURE, InputRules.dateError("01.01.2026", LocalDate.of(2025, 12, 31)))
        assertNull(InputRules.dateError("31.12.2025", LocalDate.of(2025, 12, 31)))
    }
}
