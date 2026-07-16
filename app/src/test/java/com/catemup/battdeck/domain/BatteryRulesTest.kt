package com.catemup.battdeck.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class BatteryRulesTest {
    @Test fun percentageIsCalculatedAndClamped() {
        assertEquals(0, BatteryRules.percent(35.0, 40.2, 50.2))
        assertEquals(50, BatteryRules.percent(45.2, 40.2, 50.2))
        assertEquals(100, BatteryRules.percent(60.0, 40.2, 50.2))
    }
    @Test fun statusUsesDocumentedThresholds() {
        assertEquals(BatteryStatus.READY, BatteryRules.status(95))
        assertEquals(BatteryStatus.WARNING, BatteryRules.status(50))
        assertEquals(BatteryStatus.LOW, BatteryRules.status(1))
        assertEquals(BatteryStatus.DANGER, BatteryRules.status(0))
    }
}
