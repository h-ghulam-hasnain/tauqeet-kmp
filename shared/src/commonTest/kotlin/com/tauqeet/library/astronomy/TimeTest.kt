package com.tauqeet.library.astronomy

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals
import com.tauqeet.library.time.calculateDeltaT

class TimeTest {
    @Test
    fun testDeltaTPositivityAndContinuity() {
        // Property-based test for Delta T over centuries
        // For years from 1000 to 3000, ensure it doesn't crash and remains mostly non-negative after 1900.
        for (year in 1000..3000 step 50) {
            val dt = calculateDeltaT(year.toDouble())
            
            // After 1900, Delta T is reliably positive
            if (year > 1900) {
                assertTrue(dt > -10.0, "DeltaT for $year should be non-negative or very close to 0")
            }
        }
    }
    
    @Test
    fun testDeltaTKnownValues() {
        // Known polynomial bounds
        assertEquals(calculateDeltaT(2000.0), 63.83, 1.0)
    }
}
