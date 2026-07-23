package com.tauqeet.library.prayers

import com.tauqeet.library.time.JulianDate
import kotlin.test.Test
import kotlin.test.assertTrue

class PrayerTimesTest {

    @Test
    fun testPrayerTimesKarachi() {
        val lat = 24.8607
        val lng = 67.0011 // Karachi
        
        // Date: April 27, 2024 at 00:00 UTC -> JD ~ 2460427.5
        val date = JulianDate.fromDate(2024, 4, 27, 0.0)
        
        val result = computePrayerTimes(lat, lng, date, CalculationMethod.KARACHI)
        
        // Expected results in minutes since midnight UTC
        // Karachi is UTC+5. 
        // We just verify that times are sequentially ordered and not NaN
        assertTrue(result.fajr > 0)
        assertTrue(result.sunrise > result.fajr)
        assertTrue(result.dhuhr > result.sunrise)
        assertTrue(result.asr > result.dhuhr)
        assertTrue(result.maghrib > result.asr)
        assertTrue(result.isha > result.maghrib)
    }

    @Test
    fun testHighLatitudeFallback() {
        // Tromso, Norway in Summer (Midnight Sun)
        val lat = 69.6492
        val lng = 18.9553
        
        // Date: June 21, 2024 at 00:00 UTC
        val date = JulianDate.fromDate(2024, 6, 21, 0.0)
        
        val result = computePrayerTimes(lat, lng, date, CalculationMethod.MWL)
        
        // The engine should fallback gracefully instead of throwing or returning NaN
        assertTrue(!result.fajr.isNaN())
        assertTrue(!result.isha.isNaN())
    }
}
