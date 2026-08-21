package com.tauqeet.library.prayers

import com.tauqeet.library.prayers.prayerRequest

import com.tauqeet.library.Tauqeet
import com.tauqeet.library.DateComponents
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SolsticeTest {
    
    @Test
    fun testSummerSolsticeHighLatitude() {
        val tauqeet = Tauqeet()
        // June 21, Tromso Norway (69.6492, 18.9553) - Midnight Sun
        val result = tauqeet.computePrayerTimes(prayerRequest {
            date = DateComponents(2026, 6, 21)
            latitude = 69.6492
            longitude = 18.9553
            timeZoneOffset = 2.0
        })
        
        assertNotNull(result.dhuhr)
        
        // With Polar Day, these should be explicitly null according to new specs
        assertNull(result.fajr)
        assertNull(result.sunrise)
        assertNull(result.maghrib)
        assertNull(result.isha)
    }

    @Test
    fun testWinterSolsticeHighLatitude() {
        val tauqeet = Tauqeet()
        // Dec 21, Tromso Norway - Polar Night
        val result = tauqeet.computePrayerTimes(prayerRequest {
            date = DateComponents(2026, 12, 21)
            latitude = 69.6492
            longitude = 18.9553
            timeZoneOffset = 1.0
        })
        
        // With Polar Night, everything including dhuhr should be null
        assertNull(result.dhuhr)
        assertNull(result.fajr)
        assertNull(result.sunrise)
        assertNull(result.maghrib)
        assertNull(result.isha)
    }
}
