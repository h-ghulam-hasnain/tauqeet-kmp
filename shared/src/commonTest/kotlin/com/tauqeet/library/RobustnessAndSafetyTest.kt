package com.tauqeet.library

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.random.Random

class RobustnessAndSafetyTest {

    @Test
    fun testExtremeLatitudeLongitudeBounds() {
        val tauqeet = Tauqeet()

        // NaN and Infinity bounds should throw TauqeetException
        assertFailsWith<TauqeetException> {
            tauqeet.computePrayerTimes(2026, 1, 1, Double.NaN, 0.0)
        }
        assertFailsWith<TauqeetException> {
            tauqeet.computePrayerTimes(2026, 1, 1, 0.0, Double.NaN)
        }
        assertFailsWith<TauqeetException> {
            tauqeet.computePrayerTimes(2026, 1, 1, Double.POSITIVE_INFINITY, 0.0)
        }
        assertFailsWith<TauqeetException> {
            tauqeet.computePrayerTimes(2026, 1, 1, 0.0, Double.NEGATIVE_INFINITY)
        }
    }

    @Test
    fun testDateTimeBoundaries() {
        val tauqeet = Tauqeet()

        // Year 0 or Negative throws TauqeetException cleanly
        assertFailsWith<TauqeetException> {
            tauqeet.computePrayerTimes(0, 1, 1, 21.0, 39.0)
        }
        assertFailsWith<TauqeetException> {
            tauqeet.computePrayerTimes(-500, 1, 1, 21.0, 39.0)
        }

        // Century boundaries (Should be valid and compute successfully)
        assertNotNull(tauqeet.computePrayerTimes(2000, 1, 1, 21.0, 39.0))
        assertNotNull(tauqeet.computePrayerTimes(2100, 1, 1, 21.0, 39.0))
        
        // Far future/past valid years
        assertNotNull(tauqeet.computePrayerTimes(1000, 1, 1, 21.0, 39.0))
        assertNotNull(tauqeet.computePrayerTimes(3000, 1, 1, 21.0, 39.0))
    }

    @Test
    fun testTimezoneEdgeCases() {
        val tauqeet = Tauqeet()

        // NaN or Infinity should throw
        assertFailsWith<TauqeetException> {
            tauqeet.computePrayerTimes(2026, 1, 1, 21.0, 39.0, Double.NaN)
        }
        assertFailsWith<TauqeetException> {
            tauqeet.computePrayerTimes(2026, 1, 1, 21.0, 39.0, Double.POSITIVE_INFINITY)
        }
        
        // Fractional Half-hour timezone is perfectly valid
        assertNotNull(tauqeet.computePrayerTimes(2026, 1, 1, 21.0, 39.0, 5.5))
        assertNotNull(tauqeet.computePrayerTimes(2026, 1, 1, 21.0, 39.0, 5.75))
    }

    @Test
    fun testFuzzingAndStress() {
        val tauqeet = Tauqeet()
        val random = Random(42) // Fixed seed for reproducible tests
        
        var dhuhrNulls = 0
        var total = 0
        
        // 10,000 iterations to catch random crashes or unhandled NaN propagations
        for (i in 0 until 10000) {
            val lat = random.nextDouble(-90.0, 90.0)
            val lng = random.nextDouble(-180.0, 180.0)
            val month = random.nextInt(1, 13)
            val day = random.nextInt(1, 29) // safe day for all months
            val year = random.nextInt(1900, 2100)
            val tz = random.nextDouble(-12.0, 14.0)
            
            try {
                val res = tauqeet.computePrayerTimes(year, month, day, lat, lng, tz)
                if (res.dhuhr == null) {
                    dhuhrNulls++
                }
                total++
            } catch (e: Exception) {
                // Should never throw unhandled non-Tauqeet exceptions on valid bounds
                throw AssertionError("Crashed with inputs: $year-$month-$day $lat,$lng tz=$tz", e)
            }
        }
        
        assertTrue(total == 10000, "Stress test failed to complete all iterations")
    }
}
