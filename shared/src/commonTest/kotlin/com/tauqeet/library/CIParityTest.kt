package com.tauqeet.library

import com.tauqeet.library.prayers.prayerRequest

import kotlin.test.Test
import kotlin.test.assertNotNull

class CIParityTest {

    @Test
    fun testReferenceCityMecca() {
        val tauqeet = Tauqeet()
        // Mecca (21.4225, 39.8262), UTC+3
        val result = tauqeet.computePrayerTimes(prayerRequest {
            date = DateComponents(2026, 8, 1)
            latitude = 21.4225
            longitude = 39.8262
            timeZoneOffset = 3.0
        })
        assertNotNull(result.fajr, "Fajr should not be null in Mecca")
        assertNotNull(result.dhuhr, "Dhuhr should not be null in Mecca")
        assertNotNull(result.maghrib, "Maghrib should not be null in Mecca")
        assertNotNull(result.isha, "Isha should not be null in Mecca")
    }

    @Test
    fun testReferenceCityLondon() {
        val tauqeet = Tauqeet()
        // London (51.5072, -0.1276), UTC+1
        val result = tauqeet.computePrayerTimes(prayerRequest {
            date = DateComponents(2026, 8, 1)
            latitude = 51.5072
            longitude = -0.1276
            timeZoneOffset = 1.0
        })
        assertNotNull(result.fajr, "Fajr should not be null in London")
        assertNotNull(result.dhuhr, "Dhuhr should not be null in London")
        assertNotNull(result.maghrib, "Maghrib should not be null in London")
    }

    @Test
    fun testReferenceCityTokyo() {
        val tauqeet = Tauqeet()
        // Tokyo (35.6764, 139.6500), UTC+9
        val result = tauqeet.computePrayerTimes(prayerRequest {
            date = DateComponents(2026, 8, 1)
            latitude = 35.6764
            longitude = 139.6500
            timeZoneOffset = 9.0
        })
        assertNotNull(result.fajr, "Fajr should not be null in Tokyo")
        assertNotNull(result.dhuhr, "Dhuhr should not be null in Tokyo")
        assertNotNull(result.maghrib, "Maghrib should not be null in Tokyo")
    }

    @Test
    fun testReferenceCityOslo() {
        val tauqeet = Tauqeet()
        // Oslo (59.9139, 10.7522), UTC+2
        val result = tauqeet.computePrayerTimes(prayerRequest {
            date = DateComponents(2026, 8, 1)
            latitude = 59.9139
            longitude = 10.7522
            timeZoneOffset = 2.0
        })
        // High latitude region, Dhuhr must exist.
        assertNotNull(result.dhuhr, "Dhuhr should not be null in Oslo")
    }

    @Test
    fun testReferenceCityNewYork() {
        val tauqeet = Tauqeet()
        // New York (40.7128, -74.0060), UTC-4
        val result = tauqeet.computePrayerTimes(prayerRequest {
            date = DateComponents(2026, 8, 1)
            latitude = 40.7128
            longitude = -74.0060
            timeZoneOffset = -4.0
        })
        assertNotNull(result.fajr, "Fajr should not be null in New York")
        assertNotNull(result.dhuhr, "Dhuhr should not be null in New York")
        assertNotNull(result.maghrib, "Maghrib should not be null in New York")
    }

    @Test
    fun testPolarRegionEdgeCases() {
        val tauqeet = Tauqeet()
        // Svalbard (78.2232, 15.6267) UTC+2
        // Summer (June 21) - Polar Day (Midnight Sun)
        val summerResult = tauqeet.computePrayerTimes(prayerRequest {
            date = DateComponents(2026, 6, 21)
            latitude = 78.2232
            longitude = 15.6267
            timeZoneOffset = 2.0
        })
        assertNotNull(summerResult.dhuhr, "Dhuhr should exist even in Polar Day")
        
        // Winter (Dec 21) - Polar Night
        val winterResult = tauqeet.computePrayerTimes(prayerRequest {
            date = DateComponents(2026, 12, 21)
            latitude = 78.2232
            longitude = 15.6267
            timeZoneOffset = 2.0
        })
        assertNotNull(winterResult.dhuhr, "Dhuhr should exist even in Polar Night")
    }
}
