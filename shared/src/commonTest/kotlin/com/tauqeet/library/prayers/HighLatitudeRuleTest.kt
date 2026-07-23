package com.tauqeet.library.prayers

import com.tauqeet.library.time.dateToJulianDay
import com.tauqeet.library.prayers.computePrayerTimes
import com.tauqeet.library.prayers.CalculationMethod
import com.tauqeet.library.prayers.Madhab
import com.tauqeet.library.prayers.HighLatitudeRule
import kotlin.test.Test
import kotlin.test.assertTrue

class HighLatitudeRuleTest {
    @Test
    fun testHighLatitudeFallbackStrategies() {
        // Test High Latitude fallback above Arctic circle
        // Location: Tromsø, Norway (69.6492)
        val lat = 69.6492
        val lng = 18.9553
        
        // Date: Midnight sun (June 21)
        val jd = dateToJulianDay(2024, 6, 21.0)
        
        val methods = listOf(
            HighLatitudeRule.MIDDLE_OF_NIGHT,
            HighLatitudeRule.SEVENTH_OF_NIGHT,
            HighLatitudeRule.TWILIGHT_ANGLE
        )
        
        for (rule in methods) {
            val result = computePrayerTimes(lat, lng, jd, CalculationMethod.MWL, Madhab.SHAFI, rule)
            
            assertTrue(!result.fajr.isNaN(), "Fajr should not be NaN for $rule")
            assertTrue(!result.isha.isNaN(), "Isha should not be NaN for $rule")
            
            // Just normal check to ensure the engine completed
            assertTrue(result.sunrise > 0)
        }
    }
}
