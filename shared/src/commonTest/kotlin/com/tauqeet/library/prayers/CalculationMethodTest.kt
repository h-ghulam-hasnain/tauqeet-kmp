package com.tauqeet.library.prayers

import kotlin.test.Test
import kotlin.test.assertEquals
import com.tauqeet.library.prayers.CalculationMethod

class CalculationMethodTest {
    @Test
    fun testMethodParameters() {
        val mwl = CalculationMethod.MWL.params
        assertEquals(18.0, mwl.fajrAngle, 1e-9)
        assertEquals(17.0, mwl.ishaAngle, 1e-9)
        assertEquals(0, mwl.ishaInterval)
        
        val isna = CalculationMethod.ISNA.params
        assertEquals(15.0, isna.fajrAngle, 1e-9)
        assertEquals(15.0, isna.ishaAngle, 1e-9)
        
        val makkah = CalculationMethod.MAKKAH.params
        assertEquals(18.5, makkah.fajrAngle, 1e-9)
        assertEquals(90, makkah.ishaInterval)
        
        val egypt = CalculationMethod.EGYPT.params
        assertEquals(19.5, egypt.fajrAngle, 1e-9)
        assertEquals(17.5, egypt.ishaAngle, 1e-9)
    }
}
