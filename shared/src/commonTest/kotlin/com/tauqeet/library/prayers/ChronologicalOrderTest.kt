package com.tauqeet.library.prayers

import com.tauqeet.library.prayers.prayerRequest

import com.tauqeet.library.time.dateToJulianDay
import com.tauqeet.library.Tauqeet
import com.tauqeet.library.prayers.CalculationMethod
import com.tauqeet.library.prayers.Madhab
import com.tauqeet.library.prayers.HighLatitudeRule
import kotlin.test.Test
import kotlin.test.assertTrue
import com.tauqeet.library.DateComponents
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
                val result = Tauqeet().computePrayerTimes(prayerRequest {
                    this.date = DateComponents(year, 6, 21)
                    this.latitude = lat
                    this.longitude = lng
                    this.timeZoneOffset = 0.0
                })
                
                // Normalizing to local day
                val msPerDay = 86400000L
                val fajrLocal = result.fajr?.let { (it + msPerDay) % msPerDay }
                val sunriseLocal = result.sunrise?.let { (it + msPerDay) % msPerDay }
                val dhahwaKubraLocal = result.dhahwaKubra?.let { (it + msPerDay) % msPerDay }
                val dhuhrLocal = result.dhuhr?.let { (it + msPerDay) % msPerDay }
                val asrLocal = result.asr?.let { (it + msPerDay) % msPerDay }
                val maghribLocal = result.maghrib?.let { (it + msPerDay) % msPerDay }
                var ishaLocal = result.isha?.let { (it + msPerDay) % msPerDay }
                
                if (ishaLocal != null && maghribLocal != null && ishaLocal < maghribLocal) {
                    ishaLocal += msPerDay
                }
                
                if (fajrLocal != null && sunriseLocal != null) {
                    assertTrue(fajrLocal < sunriseLocal, "Fajr ($fajrLocal) must be before Sunrise ($sunriseLocal) for lat=$lat")
                }
                if (sunriseLocal != null && dhahwaKubraLocal != null) {
                    assertTrue(sunriseLocal < dhahwaKubraLocal, "Sunrise ($sunriseLocal) must be before Dhahwa Kubra ($dhahwaKubraLocal) for lat=$lat")
                }
                if (dhahwaKubraLocal != null && dhuhrLocal != null) {
                    assertTrue(dhahwaKubraLocal < dhuhrLocal, "Dhahwa Kubra ($dhahwaKubraLocal) must be before Dhuhr ($dhuhrLocal) for lat=$lat")
                }
                if (dhuhrLocal != null && asrLocal != null) {
                    assertTrue(dhuhrLocal < asrLocal, "Dhuhr ($dhuhrLocal) must be before Asr ($asrLocal) for lat=$lat")
                }
                if (asrLocal != null && maghribLocal != null) {
                    assertTrue(asrLocal < maghribLocal, "Asr ($asrLocal) must be before Maghrib ($maghribLocal) for lat=$lat")
                }
                if (maghribLocal != null && ishaLocal != null) {
                    assertTrue(maghribLocal < ishaLocal, "Maghrib ($maghribLocal) must be before Isha ($ishaLocal) for lat=$lat")
                }
            }
        }
    }
}
