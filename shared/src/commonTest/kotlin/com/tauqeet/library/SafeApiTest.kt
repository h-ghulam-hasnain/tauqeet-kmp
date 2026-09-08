package com.tauqeet.library

import com.tauqeet.library.SafeResult
import com.tauqeet.library.prayers.prayerRequest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
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
        assertTrue(result is SafeResult.Success<*>)
        assertNotNull(result.getOrNull()?.fajr)
    }

    @Test
    fun testInvalidLatitudeReturnsFailure() {
        val result = getPrayerTimesSafe {
            latitude = 95.0
            longitude = 67.0011
            date = DateComponents(2024, 4, 27)
        }
        assertTrue(result is SafeResult.Failure)
        assertEquals(TauqeetErrorCode.INVALID_LATITUDE, (result as SafeResult.Failure).errorCode)
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
        assertEquals(TauqeetErrorCode.INVALID_LONGITUDE, (result as SafeResult.Failure).errorCode)
    }

    @Test
    fun testInvalidDateReturnsFailure() {
        val result = getPrayerTimesSafe {
            latitude = 24.8607
            longitude = 67.0011
            date = DateComponents(2024, 13, 27) // Invalid month
        }
        assertTrue(result is SafeResult.Failure)
        assertEquals(TauqeetErrorCode.INVALID_DATE, (result as SafeResult.Failure).errorCode)
    }
}
