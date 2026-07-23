package com.tauqeet.library.qibla

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertNotNull
import kotlin.test.assertEquals
import com.tauqeet.library.qibla.bearingToMecca

class QiblaTest {
    @Test
    fun testQiblaBearingNormalization() {
        // Property-based test: Qibla bearing normalization
        // Ensure qiblaBearing returns a value in [0, 360) for any valid latitude/longitude
        
        val testLocations = listOf(
            Pair(24.8607, 67.0011),    // Karachi
            Pair(51.5072, -0.1276),    // London
            Pair(40.7128, -74.0060),   // New York
            Pair(-33.8688, 151.2093),  // Sydney
            Pair(0.0, 0.0),            // Null Island
            Pair(90.0, 0.0),           // North Pole
            Pair(-90.0, 0.0)           // South Pole
        )
        
        for ((lat, lng) in testLocations) {
            val qibla = bearingToMecca(lat, lng)
            assertNotNull(qibla, "Qibla should not be null for lat=$lat, lng=$lng")
            assertTrue(qibla >= 0.0 && qibla < 360.0, "Qibla bearing $qibla out of bounds for lat=$lat, lng=$lng")
        }
    }
    
    @Test
    fun testKnownQiblaValues() {
        // Known values test
        val london = bearingToMecca(51.5072, -0.1276)
        assertNotNull(london)
        assertEquals(118.9, london, 1.0) // Approx 119 degrees
        
        val karachi = bearingToMecca(24.8607, 67.0011)
        assertNotNull(karachi)
        assertEquals(267.7, karachi, 1.0)
    }
}
