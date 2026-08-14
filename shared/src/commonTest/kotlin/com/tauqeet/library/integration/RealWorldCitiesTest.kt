package com.tauqeet.library.integration

import com.tauqeet.library.prayers.prayerRequest

import com.tauqeet.library.Tauqeet
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RealWorldCitiesTest {

    @Test
    fun testCities() {
        val tauqeet = Tauqeet()
        val year = 2026
        val month = 8
        val day = 15

        // Karachi (Standard)
        val karachi = tauqeet.computePrayerTimes(prayerRequest {
            date = DateComponents(year, month, day)
            latitude = 24.8607
            longitude = 67.0011
            timeZoneOffset = 5.0
        })
        assertNotNull(karachi.fajr)
        assertNotNull(karachi.dhuhr)
        assertNotNull(karachi.maghrib)
        assertTrue(karachi.fajr!! < karachi.dhuhr!!)
        assertTrue(karachi.dhuhr!! < karachi.maghrib!!)

        // London (High Latitude)
        val london = tauqeet.computePrayerTimes(prayerRequest {
            date = DateComponents(year, month, day)
            latitude = 51.5072
            longitude = -0.1276
            timeZoneOffset = 1.0
        })
        assertNotNull(london.fajr)
        assertNotNull(london.dhuhr)
        assertNotNull(london.maghrib)
        assertTrue(london.fajr!! < london.dhuhr!!)
        assertTrue(london.dhuhr!! < london.maghrib!!)

        // Tokyo (Far East)
        val tokyo = tauqeet.computePrayerTimes(prayerRequest {
            date = DateComponents(year, month, day)
            latitude = 35.6762
            longitude = 139.6503
            timeZoneOffset = 9.0
        })
        assertNotNull(tokyo.fajr)
        assertNotNull(tokyo.dhuhr)
        assertNotNull(tokyo.maghrib)
        assertTrue(tokyo.fajr!! < tokyo.dhuhr!!)
        assertTrue(tokyo.dhuhr!! < tokyo.maghrib!!)

        // Mecca (Near equator)
        val mecca = tauqeet.computePrayerTimes(prayerRequest {
            date = DateComponents(year, month, day)
            latitude = 21.4225
            longitude = 39.8262
            timeZoneOffset = 3.0
        })
        assertNotNull(mecca.fajr)
        assertNotNull(mecca.dhuhr)
        assertNotNull(mecca.maghrib)
        assertTrue(mecca.fajr!! < mecca.dhuhr!!)
        assertTrue(mecca.dhuhr!! < mecca.maghrib!!)

        // Oslo (Extreme High Latitude)
        val oslo = tauqeet.computePrayerTimes(prayerRequest {
            date = DateComponents(year, month, day)
            latitude = 59.9139
            longitude = 10.7522
            timeZoneOffset = 2.0
        })
        assertNotNull(oslo.fajr)
        assertNotNull(oslo.dhuhr)
        assertNotNull(oslo.maghrib)
        assertTrue(oslo.fajr!! < oslo.dhuhr!!)
        assertTrue(oslo.dhuhr!! < oslo.maghrib!!)
    }
}
