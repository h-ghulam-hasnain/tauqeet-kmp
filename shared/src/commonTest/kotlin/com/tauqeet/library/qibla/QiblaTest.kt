package com.tauqeet.library.qibla

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertNotNull
import kotlin.test.assertEquals
import kotlin.test.assertNull
import com.tauqeet.library.qibla.bearingToMecca
import com.tauqeet.library.qibla.tauqeetQibla

class QiblaTest {
    @Test
    fun testQiblaBearingNormalization() {
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
            val qibla = tauqeetQibla(lat, lng)
            assertNotNull(qibla, "Qibla should not be null for lat=$lat, lng=$lng")
            assertTrue(qibla.bearing >= 0.0 && qibla.bearing < 360.0, "Qibla bearing ${qibla.bearing} out of bounds for lat=$lat, lng=$lng")
            assertTrue(qibla.distanceKm > 0.0, "Qibla distance ${qibla.distanceKm} should be positive")
        }
    }
    
    @Test
    fun testKnownQiblaValues() {
        val london = tauqeetQibla(51.5072, -0.1276)
        assertNotNull(london)
        assertEquals(118.9, london.bearing, 1.0)
        assertEquals(4798.0, london.distanceKm, 10.0) // Distance from London to Mecca is ~4798 km
        
        val karachi = tauqeetQibla(24.8607, 67.0011)
        assertNotNull(karachi)
        assertEquals(267.7, karachi.bearing, 1.0)
        assertEquals(2804.0, karachi.distanceKm, 20.0)
    }

    @Test
    fun testAntipodalFallback() {
        // Exact Antipode of Mecca (Latitude: -21.422487, Longitude: -140.173794)
        // Vincenty fails here, but Spherical Law of Cosines should yield a valid bearing
        val antipode = tauqeetQibla(-21.422487, -140.173794)
        assertNotNull(antipode, "Antipode should fallback and not return null")
        assertTrue(antipode.bearing >= 0.0 && antipode.bearing <= 360.0, "Bearing should be valid")
        // Bearing at exact antipode is mathematically undefined; any value in [0,360) is acceptable from the fallback
        // The distance should be roughly half Earth's circumference (approx 20015 km)
        assertEquals(20015.0, antipode.distanceKm, 100.0)
    }

    @Test
    fun testCoincidentLocation() {
        // Exactly at Mecca
        val mecca = tauqeetQibla(21.422487, 39.826206)
        assertNull(mecca, "Should return null for coincident coordinates (distance < 0.001 km)")
    }
}
