package com.tauqeet.library.integration

import com.tauqeet.library.prayers.prayerRequest

import com.tauqeet.library.Tauqeet
import com.tauqeet.library.prayers.CalculationMethod
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

class MetadataAndApiTest {
    
    @Test
    fun testMetadataStructureAndPropagation() {
        val tauqeet = Tauqeet(method = CalculationMethod.KARACHI)
        
        // Use true for advanced metadata
        val result = tauqeet.computePrayerTimes(prayerRequest {
            date = DateComponents(2026, 8, 15)
            latitude = 24.8607
            longitude = 67.0011
            timeZoneOffset = 5.0
            includeAdvancedMetadata = true
        })
        
        // Assert basic metadata
        assertNotNull(result.metadata)
        assertEquals("KARACHI", result.metadata?.method)
        assertEquals("HANAFI", result.metadata?.madhab)
        assertEquals("MIDDLE_OF_NIGHT", result.metadata?.highLatitudeRule)
        assertEquals(false, result.metadata?.isPolarDay)
        assertEquals(false, result.metadata?.isPolarNight)
        
        // Assert astronomical metadata exists
        assertNotNull(result.astronomicalMetadata)
        
        // Assert Dhuhr metadata
        val dhuhrMeta = result.astronomicalMetadata?.dhuhr
        assertNotNull(dhuhrMeta)
        assertEquals(com.tauqeet.library.prayers.PrayerStatus.SUCCESS, dhuhrMeta?.status)
        
        // Assert Fajr metadata
        val fajrMeta = result.astronomicalMetadata?.fajr
        assertNotNull(fajrMeta)
        assertEquals(com.tauqeet.library.prayers.PrayerStatus.SUCCESS, fajrMeta?.status)
        
        // Flags
        assertEquals(false, result.isPolarDay)
        assertEquals(false, result.isPolarNight)
        assertEquals(false, result.isHighLatitudeFallback)
    }

    @Test
    fun testHighLatitudeMetadataPropagation() {
        val tauqeet = Tauqeet()
        
        // Tromsø, Norway in Summer (Midnight Sun)
        val result = tauqeet.computePrayerTimes(prayerRequest {
            date = DateComponents(2026, 6, 21)
            latitude = 69.6492
            longitude = 18.9553
            timeZoneOffset = 2.0
            includeAdvancedMetadata = true
        })
        
        assertNotNull(result.metadata)
        assertTrue(result.isPolarDay || result.isHighLatitudeFallback)
        // Also check that it matches the metadata object if it exists there
        if (result.metadata?.isPolarDay != null) {
            assertEquals(result.isPolarDay, result.metadata?.isPolarDay)
        }
    }
}
