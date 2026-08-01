package com.tauqeet.library.prayers

import com.tauqeet.library.DateComponents
import kotlin.test.Test
import kotlin.test.assertTrue

class HighLatitudeTest {
    @Test
    fun testHighLatitudePartialResolutionDoesNotReturnNull() {
        val request = prayerRequest {
            latitude = 69.6492
            longitude = 18.9553
            date = DateComponents(2026, 6, 21)
            timeZoneOffset = 2.0
        }

        val result = computePrayerTimes(
            lat = request.latitude,
            lng = request.longitude,
            jd = com.tauqeet.library.time.dateToJulianDay(request.date.year, request.date.month, request.date.day.toDouble()) - request.timeZoneOffset / 24.0,
            methodParams = request.calculationParameters.customMethodParams ?: request.calculationParameters.method.params
        )

        assertTrue(result.dhuhr != null, "Dhuhr should be calculated")
        assertTrue(result.isHighLatitudeFallback, "Metadata flag should record high-latitude fallback")
    }
}
