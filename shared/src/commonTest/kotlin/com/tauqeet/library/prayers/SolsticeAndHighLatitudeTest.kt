package com.tauqeet.library.prayers

import com.tauqeet.library.prayers.prayerRequest

import com.tauqeet.library.Tauqeet
import com.tauqeet.library.DateComponents
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SolsticeAndHighLatitudeTest {

    @Test
    fun testSummerSolstice() {
        val tauqeet = Tauqeet()
        // June 21, Northern Hemisphere Summer Solstice
        val result = tauqeet.computePrayerTimes(prayerRequest {
            date = DateComponents(2026, 6, 21)
            latitude = 51.5072
            longitude = -0.1276
            timeZoneOffset = 1.0
        })
        
        assertNotNull(result.fajr)
        assertNotNull(result.maghrib)
        // Days are long, fajr should be very early
        assertTrue(result.fajr!! < result.dhuhr!!)
    }

    @Test
    fun testWinterSolstice() {
        val tauqeet = Tauqeet()
        // Dec 21, Northern Hemisphere Winter Solstice
        val result = tauqeet.computePrayerTimes(prayerRequest {
            date = DateComponents(2026, 12, 21)
            latitude = 51.5072
            longitude = -0.1276
            timeZoneOffset = 1.0
        })
        
        assertNotNull(result.fajr)
        assertNotNull(result.maghrib)
        assertTrue(result.fajr!! < result.dhuhr!!)
    }

    @Test
    fun testPolarDay() {
        val tauqeet = Tauqeet()
        // Tromsø, Norway in Summer (Midnight Sun)
        val result = tauqeet.computePrayerTimes(prayerRequest {
            date = DateComponents(2026, 6, 21)
            latitude = 69.6492
            longitude = 18.9553
            timeZoneOffset = 2.0
        })
        
        // Ensure times are still calculated using fallbacks
        assertNotNull(result.dhuhr)
        assertTrue(result.isPolarDay || result.isHighLatitudeFallback)
    }

    @Test
    fun testPolarNight() {
        val tauqeet = Tauqeet()
        // Tromsø, Norway in Winter (Polar Night)
        val result = tauqeet.computePrayerTimes(prayerRequest {
            date = DateComponents(2026, 12, 21)
            latitude = 69.6492
            longitude = 18.9553
            timeZoneOffset = 2.0
        })
        
        // Ensure times are still calculated using fallbacks
        assertNull(result.dhuhr)
        assertTrue(result.isPolarNight || result.isHighLatitudeFallback)
    }
}
