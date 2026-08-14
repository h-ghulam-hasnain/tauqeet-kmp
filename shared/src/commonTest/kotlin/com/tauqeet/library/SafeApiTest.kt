package com.tauqeet.library

import com.tauqeet.library.prayers.SafeResult
import com.tauqeet.library.prayers.prayerRequest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SafeApiTest {

    @Test
    fun testValidRequestReturnsSuccess() {
        val result = getPrayerTimesSafe {
            latitude = 24.8607
            longitude = 67.0011
            date = DateComponents(2024, 4, 27)
            timeZoneOffset = 5.0
        }
        assertTrue(result is SafeResult.Success)
        assertTrue(result.data.fajr != null)
    }

    @Test
    fun testInvalidLatitudeReturnsFailure() {
        val result = getPrayerTimesSafe {
            latitude = 95.0
            longitude = 67.0011
            date = DateComponents(2024, 4, 27)
        }
        assertTrue(result is SafeResult.Failure)
        assertEquals("INVALID_LATITUDE", result.code)
        assertTrue(result.message.contains("Invalid latitude"))
    }

    @Test
    fun testInvalidLongitudeReturnsFailure() {
        val result = getPrayerTimesSafe {
            latitude = 24.8607
            longitude = 185.0
            date = DateComponents(2024, 4, 27)
        }
        assertTrue(result is SafeResult.Failure)
        assertEquals("INVALID_LONGITUDE", result.code)
    }

    @Test
    fun testInvalidDateReturnsFailure() {
        val result = getPrayerTimesSafe {
            latitude = 24.8607
            longitude = 67.0011
            date = DateComponents(2024, 13, 27) // Invalid month
        }
        assertTrue(result is SafeResult.Failure)
        assertEquals("DATE_OUT_OF_RANGE", result.code)
    }
}
