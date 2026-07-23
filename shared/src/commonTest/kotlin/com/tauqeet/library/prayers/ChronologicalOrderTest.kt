package com.tauqeet.library.prayers

import com.tauqeet.library.time.dateToJulianDay
import com.tauqeet.library.prayers.computePrayerTimes
import com.tauqeet.library.prayers.CalculationMethod
import com.tauqeet.library.prayers.Madhab
import com.tauqeet.library.prayers.HighLatitudeRule
import kotlin.test.Test
import kotlin.test.assertTrue

class ChronologicalOrderTest {
    @Test
    fun testChronologicalOrderInvariant() {
        // Property-based test for chronological ordering
        // For any valid date and reasonable latitude, ensure fajr < sunrise < dhuhr < asr < maghrib < isha
        val years = listOf(1900, 2000, 2100)
        val latitudes = listOf(-60.0, -30.0, 0.0, 30.0, 60.0)
        
        for (year in years) {
            val jd = dateToJulianDay(year, 6, 21.0)
            
            for (lat in latitudes) {
                // Ensure times are chronologically ordered in local mean time.
                // We add tzOffset corresponding to local mean time to avoid negative UTC wrap-arounds.
                val lng = 0.0 // Arbitrary longitude for LMT test
                val result = computePrayerTimes(lat, lng, jd, CalculationMethod.MWL, Madhab.SHAFI, HighLatitudeRule.MIDDLE_OF_NIGHT)
                
                // Normalizing to local day
                val fajrLocal = (result.fajr + 1440) % 1440
                val sunriseLocal = (result.sunrise + 1440) % 1440
                val dhuhrLocal = (result.dhuhr + 1440) % 1440
                val asrLocal = (result.asr + 1440) % 1440
                val maghribLocal = (result.maghrib + 1440) % 1440
                var ishaLocal = (result.isha + 1440) % 1440
                
                if (ishaLocal < maghribLocal) ishaLocal += 1440.0
                
                assertTrue(fajrLocal < sunriseLocal, "Fajr ($fajrLocal) must be before Sunrise ($sunriseLocal) for lat=$lat")
                assertTrue(sunriseLocal < dhuhrLocal, "Sunrise ($sunriseLocal) must be before Dhuhr ($dhuhrLocal) for lat=$lat")
                assertTrue(dhuhrLocal < asrLocal, "Dhuhr ($dhuhrLocal) must be before Asr ($asrLocal) for lat=$lat")
                assertTrue(asrLocal < maghribLocal, "Asr ($asrLocal) must be before Maghrib ($maghribLocal) for lat=$lat")
                assertTrue(maghribLocal < ishaLocal, "Maghrib ($maghribLocal) must be before Isha ($ishaLocal) for lat=$lat")
            }
        }
    }
}
