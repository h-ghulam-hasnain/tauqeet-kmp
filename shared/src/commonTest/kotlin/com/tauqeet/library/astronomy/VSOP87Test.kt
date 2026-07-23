package com.tauqeet.library.astronomy

import com.tauqeet.library.internal.normalizeDegrees
import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertEquals

class VSOP87Test {
    @Test
    fun testVSOP87DEarthHeliocentricCoordinatesValidation() {
        // JD = 2448908.5
        // te (Julian centuries) = -0.072183436
        // tau (Julian millennia) = te / 10 = -0.0072183436
        val tau = -0.0072183436

        val state = computeEarthHeliocentricState(tau)

        // Convert coordinates to degrees
        val L_deg = normalizeDegrees((state.longitude * 180.0) / PI)
        val B_deg = (state.latitude * 180.0) / PI

        // Expected values from VSOP87D full theory for Earth:
        // L = 19.907297 degrees (referred to FK5)
        // B = -0.000179 degrees
        // R = 0.99760853 AU
        assertEquals(19.907297, L_deg, 1e-4)
        assertEquals(-0.000179, B_deg, 1e-4)
        assertEquals(0.99760853, state.radius, 1e-6)
    }
}
