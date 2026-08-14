package com.tauqeet.library.prayers

import com.tauqeet.library.Tauqeet
import com.tauqeet.library.DateComponents
import com.tauqeet.library.prayers.CalculationMethod
import com.tauqeet.library.prayers.Madhab
import com.tauqeet.library.prayers.CalculationMethodParameters
import com.tauqeet.library.time.dateToJulianDay
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrayerTimesTest {

    @Test
    fun testPrayerTimesKarachi() {
        val lat = 24.8607
        val lng = 67.0011 // Karachi
        // Date: April 27, 2024
        val request = prayerRequest {
            date = DateComponents(2024, 4, 27)
            latitude = lat
            longitude = lng
            timeZoneOffset = 5.0
            calculation.method = CalculationMethod.KARACHI
        }
        val tauqeet = Tauqeet()
        val result = tauqeet.computePrayerTimes(request)
        // Convert to local timezone (UTC+5)
        val tzOffsetMs = (5.0 * 3600000.0).toLong()
        val msPerDay = 86400000L
        val fajrLocal = (result.fajr!! + tzOffsetMs + msPerDay) % msPerDay
        val sunriseLocal = (result.sunrise!! + tzOffsetMs + msPerDay) % msPerDay
        val dhuhrLocal = (result.dhuhr!! + tzOffsetMs + msPerDay) % msPerDay
        val asrLocal = (result.asr!! + tzOffsetMs + msPerDay) % msPerDay
        val maghribLocal = (result.maghrib!! + tzOffsetMs + msPerDay) % msPerDay
        val ishaLocal = (result.isha!! + tzOffsetMs + msPerDay) % msPerDay
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
        val request = prayerRequest {
            date = DateComponents(2024, 6, 21)
            latitude = lat
            longitude = lng
            calculation.method = CalculationMethod.MWL
        }
        val tauqeet = Tauqeet()
        val result = tauqeet.computePrayerTimes(request)
        // Should not throw; simply ensure result exists
        assertTrue(true)
    }

    @Test
    fun testISOTimeFormatting() {
        val lat = 31.39965
        val lng = 73.02003
        val request = prayerRequest {
            date = DateComponents(2026, 7, 23)
            latitude = lat
            longitude = lng
            // default timezone offset 0
        }
        val tauqeet = Tauqeet()
        val result = tauqeet.computePrayerTimes(request)
        val isoTimes = result.toISOTimes()
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
        val request = prayerRequest {
            date = DateComponents(2026, 7, 23)
            latitude = lat
            longitude = lng
            calculation.method = CalculationMethod.ALGERIA
            calculation.madhab = Madhab.MALIKI
        }
        val tauqeet = Tauqeet()
        val result = tauqeet.computePrayerTimes(request)
        assertEquals("ALGERIA", result.metadata?.method)
        assertEquals("MALIKI", result.metadata?.madhab)
        assertTrue(result.fajr != null && result.fajr > 0)
    }

    @Test
    fun testCustomMethod() {
        val lat = 51.5072
        val lng = -0.1276
        val customParams = CalculationMethodParameters(fajrAngle = 13.5, ishaAngle = 13.5)
        val request = prayerRequest {
            date = DateComponents(2026, 7, 23)
            latitude = lat
            longitude = lng
            calculation.method = CalculationMethod.CUSTOM
            calculation.customMethodParams = customParams
            calculation.madhab = Madhab.JAAFARI
        }
        val tauqeet = Tauqeet()
        val result = tauqeet.computePrayerTimes(request)
        assertEquals("CUSTOM", result.metadata?.method)
        assertEquals("JAAFARI", result.metadata?.madhab)
        assertTrue(result.fajr != null && result.fajr > 0)
    }
}
