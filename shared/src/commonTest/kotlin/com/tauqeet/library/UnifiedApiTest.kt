package com.tauqeet.library

import com.tauqeet.library.prayers.CalculationMethod
import com.tauqeet.library.prayers.HighLatitudeRule
import com.tauqeet.library.prayers.PrayerTimesResult
import com.tauqeet.library.prayers.SolverKind
import com.tauqeet.library.prayers.prayerRequest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UnifiedApiTest {

    @Test
    fun testPrayerRequestDslBuildsAndReturnsResult() {
        val tauqeet = Tauqeet()

        val request = prayerRequest {
            latitude = 21.4225
            longitude = 39.8262
            date = DateComponents(2026, 8, 1)
            timeZoneOffset = 3.0
            includeAdvancedMetadata = false
        }

        val result = tauqeet.computePrayerTimes(request)
        assertNotNull(result.fajr)
        assertNotNull(result.dhuhr)
        assertNotNull(result.resolutionInfo)
        assertEquals(SolverKind.NORMAL, result.resolutionInfo?.solver)
    }

    @Test
    fun testPrayerRequestDslCarriesCalculationParameters() {
        val tauqeet = Tauqeet()

        val request = prayerRequest {
            latitude = 51.5072
            longitude = -0.1276
            date = DateComponents(2026, 8, 1)
            timeZoneOffset = 1.0
            calculation {
                method = CalculationMethod.MWL
                highLatitudeRule = HighLatitudeRule.MIDDLE_OF_NIGHT
            }
        }

        val result = tauqeet.computePrayerTimes(request)
        assertNotNull(result.metadata)
        assertEquals("MWL", result.metadata?.method)
        assertEquals("MIDDLE_OF_NIGHT", result.metadata?.highLatitudeRule)
    }

    @Test
    fun testPolarDayResolutionMetadataIsPropagated() {
        val tauqeet = Tauqeet()
        val result = tauqeet.computePrayerTimes(
            prayerRequest {
                latitude = 69.6492
                longitude = 18.9553
                date = DateComponents(2026, 6, 21)
                timeZoneOffset = 1.0
            }
        )

        assertNotNull(result.resolutionInfo)
        assertTrue(result.flags and PrayerTimesResult.FLAG_POLAR_DAY != 0 || result.resolutionInfo?.solver == SolverKind.POLAR_DAY)
        assertTrue(result.metadata?.isPolarDay == true || result.isPolarDay)
    }

    @Test
    fun testPolarNightResolutionMetadataIsPropagated() {
        val tauqeet = Tauqeet()
        val result = tauqeet.computePrayerTimes(
            prayerRequest {
                latitude = 69.6492
                longitude = 18.9553
                date = DateComponents(2026, 12, 21)
                timeZoneOffset = 1.0
            }
        )

        assertNotNull(result.resolutionInfo)
        assertTrue(result.flags and PrayerTimesResult.FLAG_POLAR_NIGHT != 0 || result.resolutionInfo?.solver == SolverKind.POLAR_NIGHT)
        assertTrue(result.metadata?.isPolarNight == true || result.isPolarNight)
    }

    @Test
    fun testHighLatitudeFallbackFlagIsRaisedForEdgeCaseLatitude() {
        val tauqeet = Tauqeet()
        val result = tauqeet.computePrayerTimes(
            prayerRequest {
                latitude = 69.6492
                longitude = 18.9553
                date = DateComponents(2026, 6, 21)
                timeZoneOffset = 1.0
                calculation {
                    method = CalculationMethod.MWL
                    highLatitudeRule = HighLatitudeRule.MIDDLE_OF_NIGHT
                }
            }
        )

        assertNotNull(result.resolutionInfo)
        assertTrue(
            result.isHighLatitudeFallback || result.isPolarDay || result.isPolarNight ||
                result.resolutionInfo?.solver == SolverKind.HIGH_LATITUDE ||
                result.resolutionInfo?.solver == SolverKind.POLAR_DAY ||
                result.resolutionInfo?.solver == SolverKind.POLAR_NIGHT
        )
    }

    @Test
    @Suppress("DEPRECATION")
    fun testDeprecatedFlatOverloadsStillWork() {
        val tauqeet = Tauqeet()
        val result = tauqeet.computePrayerTimes(2026, 8, 1, 35.6764, 139.6500, 9.0)
        assertNotNull(result.dhuhr)
        assertTrue(result.resolutionInfo != null)
    }
}
