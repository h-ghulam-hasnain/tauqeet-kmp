package com.tauqeet.library.integration

import com.tauqeet.library.prayers.prayerRequest

import com.tauqeet.library.Tauqeet
import com.tauqeet.library.DateComponents
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
            this.date = DateComponents(year, month, day)
            this.latitude = 24.8607
            this.longitude = 67.0011
            this.timeZoneOffset = 5.0
        })
        assertNotNull(karachi.fajr)
        assertNotNull(karachi.dhuhr)
        assertNotNull(karachi.maghrib)
        assertTrue(karachi.fajr!! < karachi.dhuhr!!)
        assertTrue(karachi.dhuhr!! < karachi.maghrib!!)

        // London (High Latitude)
        val london = tauqeet.computePrayerTimes(prayerRequest {
            this.date = DateComponents(year, month, day)
            this.latitude = 51.5072
            this.longitude = -0.1276
            this.timeZoneOffset = 1.0
        })
        assertNotNull(london.fajr)
        assertNotNull(london.dhuhr)
        assertNotNull(london.maghrib)
        assertTrue(london.fajr!! < london.dhuhr!!)
        assertTrue(london.dhuhr!! < london.maghrib!!)

        // Tokyo (Far East)
        val tokyo = tauqeet.computePrayerTimes(prayerRequest {
            this.date = DateComponents(year, month, day)
            this.latitude = 35.6762
            this.longitude = 139.6503
            this.timeZoneOffset = 9.0
        })
        assertNotNull(tokyo.fajr)
        assertNotNull(tokyo.dhuhr)
        assertNotNull(tokyo.maghrib)
        assertTrue(tokyo.fajr!! < tokyo.dhuhr!!)
        assertTrue(tokyo.dhuhr!! < tokyo.maghrib!!)

        // Mecca (Near equator)
        val mecca = tauqeet.computePrayerTimes(prayerRequest {
            this.date = DateComponents(year, month, day)
            this.latitude = 21.4225
            this.longitude = 39.8262
            this.timeZoneOffset = 3.0
        })
        assertNotNull(mecca.fajr)
        assertNotNull(mecca.dhuhr)
        assertNotNull(mecca.maghrib)
        assertTrue(mecca.fajr!! < mecca.dhuhr!!)
        assertTrue(mecca.dhuhr!! < mecca.maghrib!!)

        // Oslo (Extreme High Latitude)
        val oslo = tauqeet.computePrayerTimes(prayerRequest {
            this.date = DateComponents(year, month, day)
            this.latitude = 59.9139
            this.longitude = 10.7522
            this.timeZoneOffset = 2.0
        })
        assertNotNull(oslo.fajr)
        assertNotNull(oslo.dhuhr)
        assertNotNull(oslo.maghrib)
        assertTrue(oslo.fajr!! < oslo.dhuhr!!)
        assertTrue(oslo.dhuhr!! < oslo.maghrib!!)
    }
}
