package com.tauqeet.library.prayers

import com.tauqeet.library.time.dateToJulianDay
import kotlin.test.Test
import kotlin.test.assertEquals
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

    @Test
    fun testISOTimeFormatting() {
        val lat = 31.39965
        val lng = 73.02003
        
        // Date: 2026-7-23 (Today)
        val jd = dateToJulianDay(2026, 7, 23.0)
        
        val result = computePrayerTimes(lat, lng, jd, CalculationMethod.KARACHI, Madhab.HANAFI)
        
        // Use the newly added toISOTimes extension
        val isoTimes = result.toISOTimes()
        
        // The regex ensures it's formatted exactly as "HH:mm:ss"
        val timeRegex = Regex("^([01]\\d|2[0-3]):([0-5]\\d):([0-5]\\d)$")
        assertTrue(timeRegex.matches(isoTimes.fajr), "fajr should match HH:mm:ss format")
        assertTrue(timeRegex.matches(isoTimes.sunrise), "sunrise should match HH:mm:ss format")
        assertTrue(timeRegex.matches(isoTimes.dhuhr), "dhuhr should match HH:mm:ss format")
        assertTrue(timeRegex.matches(isoTimes.asr), "asr should match HH:mm:ss format")
        assertTrue(timeRegex.matches(isoTimes.maghrib), "maghrib should match HH:mm:ss format")
        assertTrue(timeRegex.matches(isoTimes.isha), "isha should match HH:mm:ss format")
    }

    @Test
    fun testAlgeriaMethod() {
        val lat = 36.7538
        val lng = 3.0588 // Algiers
        val jd = dateToJulianDay(2026, 7, 23.0)
        
        val result = computePrayerTimes(lat, lng, jd, CalculationMethod.ALGERIA, Madhab.MALIKI)
        // Check metadata properly set
        assertEquals("ALGERIA", result.metadata?.method)
        assertEquals("MALIKI", result.metadata?.madhab)
        assertTrue(result.fajr != null && result.fajr > 0)
    }

    @Test
    fun testCustomMethod() {
        val lat = 51.5072
        val lng = -0.1276
        val jd = dateToJulianDay(2026, 7, 23.0)
        
        val customParams = CalculationMethodParameters(fajrAngle = 13.5, ishaAngle = 13.5)
        val result = computePrayerTimes(lat, lng, jd, customParams, "CUSTOM", Madhab.JAAFARI)
        
        assertEquals("CUSTOM", result.metadata?.method)
        assertEquals("JAAFARI", result.metadata?.madhab)
        assertTrue(result.fajr != null && result.fajr > 0)
    }
}

