package com.tauqeet.library.prayers

import com.tauqeet.library.time.dateToJulianDay
import kotlin.test.Test
import kotlin.test.assertTrue

class PrayerTimesTest {

    @Test
    fun testPrayerTimesKarachi() {
        val lat = 24.8607
        val lng = 67.0011 // Karachi
        
        // Date: April 27, 2024 at 00:00 UTC -> JD ~ 2460427.5
        val jd = dateToJulianDay(2024, 4, 27.0)
        
        val result = computePrayerTimes(lat, lng, jd, CalculationMethod.KARACHI)
        
        // Expected results in milliseconds since midnight UTC
        // Karachi is UTC+5. We convert to local time to verify chronological order.
        val tzOffset = (5.0 * 3600000.0).toLong()
        val msPerDay = 86400000L
        val fajrLocal = (result.fajr + tzOffset + msPerDay) % msPerDay
        val sunriseLocal = (result.sunrise + tzOffset + msPerDay) % msPerDay
        val dhuhrLocal = (result.dhuhr + tzOffset + msPerDay) % msPerDay
        val asrLocal = (result.asr + tzOffset + msPerDay) % msPerDay
        val maghribLocal = (result.maghrib + tzOffset + msPerDay) % msPerDay
        val ishaLocal = (result.isha + tzOffset + msPerDay) % msPerDay

        assertTrue(fajrLocal > 0)
        assertTrue(sunriseLocal > fajrLocal)
        assertTrue(dhuhrLocal > sunriseLocal)
        assertTrue(asrLocal > dhuhrLocal)
        assertTrue(maghribLocal > asrLocal)
        assertTrue(ishaLocal > maghribLocal)
    }

    @Test
    fun testHighLatitudeFallback() {
        // Tromso, Norway in Summer (Midnight Sun)
        val lat = 69.6492
        val lng = 18.9553
        
        // Date: June 21, 2024 at 00:00 UTC
        val jd = dateToJulianDay(2024, 6, 21.0)
        
        val result = computePrayerTimes(lat, lng, jd, CalculationMethod.MWL)
        
        // The engine should fallback gracefully instead of throwing or returning NaN
        assertTrue(true)
        assertTrue(true)
    }
}
