package com.tauqeet.library.astronomy

import kotlin.test.Test
import kotlin.test.assertEquals
import com.tauqeet.library.time.dateToJulianDay
import com.tauqeet.library.time.calculateDeltaT
import com.tauqeet.library.astronomy.computeSolarPosition
import kotlin.math.abs

class VSOP87Test {
    @Test
    fun testAnnualPeriodicity() {
        val lat = 24.8607
        val lng = 67.0011
        
        // Property-based test: VSOP87 solar position should be highly periodic.
        // For a random set of years, testing solar position on exact same day + 365.25 days should yield similar declination.
        val years = listOf(1990, 2000, 2024, 2050)
        
        for (year in years) {
            val jd1 = dateToJulianDay(year, 3, 21.0)
            val sp1 = computeSolarPosition(jd1, 12.0, calculateDeltaT(year.toDouble()))
            
            // 1 tropical year = 365.24219 days
            val jd2 = jd1 + 365.24219
            val sp2 = computeSolarPosition(jd2, 12.0, calculateDeltaT(year.toDouble() + 1))
            
            // Declination should be nearly identical (within small planetary perturbation tolerance ~0.05 deg max)
            assertEquals(sp1.declination, sp2.declination, 0.05)
        }
    }
}
