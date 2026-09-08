package com.tauqeet.library.prayers

import com.tauqeet.library.DateComponents
import com.tauqeet.library.Tauqeet
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class PrayerFormattingTest {

    @Test
    fun testPrayerFormattingAndNextPrayer() {
        val tauqeet = Tauqeet(method = CalculationMethod.KARACHI)
        val result = tauqeet.computePrayerTimes(
            prayerRequest {
                latitude = 24.8607
                longitude = 67.0011
                date = DateComponents(2026, 3, 15)
                timeZoneOffset = 5.0
            }
        )

        val formatted = result.format("12h")
        assertNotNull(formatted["fajr"])
        assertNotNull(formatted["dhuhr"])

        // Morning time (07:00 AM = 7 * 3600 * 1000 = 25200000 ms; after Sunrise ~06:35 AM, before Dhuhr ~12:35 PM)
        val morningMs = 7 * 3600 * 1000L
        val next = result.getNextPrayer(morningMs)
        assertNotNull(next)
        assertEquals("dhuhr", next.name)

        val remaining = result.getTimeRemaining(morningMs)
        assertNotNull(remaining)
        assertEquals("dhuhr", remaining.name)
    }
}
