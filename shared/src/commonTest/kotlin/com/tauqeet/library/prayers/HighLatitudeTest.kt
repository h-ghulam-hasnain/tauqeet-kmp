package com.tauqeet.library.prayers

import com.tauqeet.library.Tauqeet
import com.tauqeet.library.DateComponents
import com.tauqeet.library.prayers.prayerRequest

class HighLatitudeTest {
    @Test
    fun testHighLatitudePartialResolutionDoesNotReturnNull() {
        val request = prayerRequest {
            latitude = 69.6492
            longitude = 18.9553
            date = DateComponents(2026, 6, 21)
            timeZoneOffset = 2.0
        }

        val tauqeet = Tauqeet()
        val result = tauqeet.computePrayerTimes(request)

        assertTrue(result.dhuhr != null, "Dhuhr should be calculated")
        assertTrue(result.isHighLatitudeFallback, "Metadata flag should record high-latitude fallback")
    }
}
