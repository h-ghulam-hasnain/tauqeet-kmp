package com.tauqeet.library

import com.tauqeet.library.astronomy.computeSolarPosition
import com.tauqeet.library.time.calculateDeltaT
import org.junit.Test
import kotlin.math.floor

class DebugEphemTest {
    @Test
    fun testEphem() {
        val j0 = 2461120.5
        val ut = 1.5872100032866001
        
        val probeJd = j0 + ut / 24.0
        val approxYear = 2000.0 + (probeJd - 2451545.0) / 365.25
        val deltaT = calculateDeltaT(approxYear)
        
        val sp = computeSolarPosition(j0, ut, deltaT)
        
        println("KMP Ephemeris for j0=$j0 ut=$ut")
        println("declination: ${sp.declination}")
        println("equationOfTime: ${sp.equationOfTime}")
        println("semidiameter: ${sp.semidiameter}")
        println("horizontalParallax: ${sp.horizontalParallax}")
    }
}
