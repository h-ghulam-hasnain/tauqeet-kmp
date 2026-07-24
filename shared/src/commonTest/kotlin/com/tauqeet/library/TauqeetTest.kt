package com.tauqeet.library

import com.tauqeet.library.prayers.CalculationMethod
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

class TauqeetTest {
    @Test
    fun testPublicApi() {
        val tauqeet = Tauqeet(method = CalculationMethod.KARACHI)
        
        // Karachi April 27, 2024 (UTC+5)
        val result = tauqeet.computePrayerTimes(2024, 4, 27, 24.8607, 67.0011, 5.0)
        
        val fajrStr = result.fajr?.toTimeString() ?: ""
        val dhuhrStr = result.dhuhr?.toTimeString() ?: ""
        
        assertTrue(fajrStr.contains(":"))
        assertTrue(dhuhrStr.contains(":"))
        
        // Times should be sequential
        assertTrue(result.sunrise!! > result.fajr!!)
        assertTrue(result.dhuhr!! > result.sunrise!!)
        assertTrue(result.asr!! > result.dhuhr!!)
        
        // Test Qibla
        val qibla = bearingToMecca(24.8607, 67.0011)
        assertNotNull(qibla)
        assertTrue(qibla > 250.0 && qibla < 270.0) // Karachi to Makkah is approx 261 degrees
    }
}
