package com.tauqeet.library.integration

import com.tauqeet.library.Tauqeet
import com.tauqeet.library.DateComponents
import com.tauqeet.library.toTimeString
import com.tauqeet.library.prayers.CalculationMethod
import com.tauqeet.library.qibla.bearingToMecca
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

class ApiTest {
    @Test
    fun testCompletePublicApi() {
        val tauqeet = Tauqeet(method = CalculationMethod.KARACHI)
        
        val date = DateComponents(2024, 4, 27)
        val result = tauqeet.computePrayerTimes(date, 24.8607, 67.0011, 5.0)
        
        // Asserting string conversions properly map
        val fajrStr = result.fajr?.toTimeString() ?: ""
        val sunriseStr = result.sunrise?.toTimeString() ?: ""
        val ishaStr = result.isha?.toTimeString() ?: ""
        
        assertTrue(fajrStr.length == 8, "Fajr time should be formatted as HH:mm:ss")
        assertTrue(sunriseStr.contains(":"))
        
        val qibla = bearingToMecca(24.8607, 67.0011)
        assertNotNull(qibla)
        assertTrue(qibla!! > 267.0 && qibla < 268.0)
    }
}
