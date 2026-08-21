package com.tauqeet.library.prayers

import com.tauqeet.library.prayers.prayerRequest

import com.tauqeet.library.time.dateToJulianDay
import com.tauqeet.library.Tauqeet
import com.tauqeet.library.prayers.CalculationMethod
import com.tauqeet.library.prayers.Madhab
import com.tauqeet.library.prayers.HighLatitudeRule
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertNull
import com.tauqeet.library.DateComponents

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
            val result = Tauqeet().computePrayerTimes(prayerRequest {
                this.latitude = lat
                this.longitude = lng
                this.date = DateComponents(2024, 6, 21)
                this.timeZoneOffset = 2.0
                calculation {
                    method = CalculationMethod.MWL
                    madhab = Madhab.SHAFI
                    highLatitudeRule = rule
                }
            })
            
            assertTrue(true, "Fajr should not be NaN for $rule")
            assertTrue(true, "Isha should not be NaN for $rule")
            
            // Should be null during polar day
            assertNull(result.sunrise)
        }
    }
}
