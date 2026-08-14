package com.tauqeet.library

import com.tauqeet.library.prayers.prayerRequest

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
            tauqeet.computePrayerTimes(prayerRequest {
            date = DateComponents(2026, 1, 1)
            latitude = Double.NaN
            longitude = 0.0
        })
        }
        assertFailsWith<TauqeetException> {
            tauqeet.computePrayerTimes(prayerRequest {
            date = DateComponents(2026, 1, 1)
            latitude = 0.0
            longitude = Double.NaN
        })
        }
        assertFailsWith<TauqeetException> {
            tauqeet.computePrayerTimes(prayerRequest {
            date = DateComponents(2026, 1, 1)
            latitude = Double.POSITIVE_INFINITY
            longitude = 0.0
        })
        }
        assertFailsWith<TauqeetException> {
            tauqeet.computePrayerTimes(prayerRequest {
            date = DateComponents(2026, 1, 1)
            latitude = 0.0
            longitude = Double.NEGATIVE_INFINITY
        })
        }
    }

    @Test
    fun testDateTimeBoundaries() {
        val tauqeet = Tauqeet()

        // Year 0 or Negative throws TauqeetException cleanly
        assertFailsWith<TauqeetException> {
            tauqeet.computePrayerTimes(prayerRequest {
            date = DateComponents(0, 1, 1)
            latitude = 21.0
            longitude = 39.0
        })
        }
        assertFailsWith<TauqeetException> {
            tauqeet.computePrayerTimes(prayerRequest {
            date = DateComponents(-500, 1, 1)
            latitude = 21.0
            longitude = 39.0
        })
        }

        // Century boundaries (Should be valid and compute successfully)
        assertNotNull(tauqeet.computePrayerTimes(prayerRequest {
            date = DateComponents(2000, 1, 1)
            latitude = 21.0
            longitude = 39.0
        }))
        assertNotNull(tauqeet.computePrayerTimes(prayerRequest {
            date = DateComponents(2100, 1, 1)
            latitude = 21.0
            longitude = 39.0
        }))
        
        // Far future/past valid years
        assertNotNull(tauqeet.computePrayerTimes(prayerRequest {
            date = DateComponents(1000, 1, 1)
            latitude = 21.0
            longitude = 39.0
        }))
        assertNotNull(tauqeet.computePrayerTimes(prayerRequest {
            date = DateComponents(3000, 1, 1)
            latitude = 21.0
            longitude = 39.0
        }))
    }

    @Test
    fun testTimezoneEdgeCases() {
        val tauqeet = Tauqeet()

        // NaN or Infinity should throw
        assertFailsWith<TauqeetException> {
            tauqeet.computePrayerTimes(prayerRequest {
            date = DateComponents(2026, 1, 1)
            latitude = 21.0
            longitude = 39.0
            timeZoneOffset = Double.NaN
        })
        }
        assertFailsWith<TauqeetException> {
            tauqeet.computePrayerTimes(prayerRequest {
            date = DateComponents(2026, 1, 1)
            latitude = 21.0
            longitude = 39.0
            timeZoneOffset = Double.POSITIVE_INFINITY
        })
        }
        
        // Fractional Half-hour timezone is perfectly valid
        assertNotNull(tauqeet.computePrayerTimes(prayerRequest {
            date = DateComponents(2026, 1, 1)
            latitude = 21.0
            longitude = 39.0
            timeZoneOffset = 5.5
        }))
        assertNotNull(tauqeet.computePrayerTimes(prayerRequest {
            date = DateComponents(2026, 1, 1)
            latitude = 21.0
            longitude = 39.0
            timeZoneOffset = 5.75
        }))
    }

    @Test
    fun testFuzzingAndStress() {
        val tauqeet = Tauqeet()
        val random = Random(42) // Fixed seed for reproducible tests
        
        var dhuhrNulls = 0
        var total = 0
        
        // 200 iterations to catch random crashes or unhandled NaN propagations without timing out on JS
        for (i in 0 until 200) {
            val lat = random.nextDouble(-90.0, 90.0)
            val lng = random.nextDouble(-180.0, 180.0)
            val month = random.nextInt(1, 13)
            val day = random.nextInt(1, 29) // safe day for all months
            val year = random.nextInt(1900, 2100)
            val tz = random.nextDouble(-12.0, 14.0)
            
            try {
                val res = tauqeet.computePrayerTimes(prayerRequest {
            date = DateComponents(year, month, day)
            latitude = lat
            longitude = lng
            timeZoneOffset = tz
        })
                if (res.dhuhr == null) {
                    dhuhrNulls++
                }
                total++
            } catch (e: Exception) {
                // Should never throw unhandled non-Tauqeet exceptions on valid bounds
                throw AssertionError("Crashed with inputs: $year-$month-$day $lat,$lng tz=$tz", e)
            }
        }
        
        assertTrue(total == 200, "Stress test failed to complete all iterations")
    }
}
