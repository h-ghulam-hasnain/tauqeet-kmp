package com.tauqeet.library

import com.tauqeet.library.prayers.CalculationMethod
import com.tauqeet.library.prayers.Madhab
import com.tauqeet.library.qibla.bearingToMecca
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertEquals

class TauqeetTest {

    // ─────────────────────────────────────────────────────────────────────────
    // Existing sanity test (unchanged)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun testPublicApi() {
        val tauqeet = Tauqeet(method = CalculationMethod.KARACHI)

        // Karachi April 27, 2024 (UTC+5)
        val result = tauqeet.computePrayerTimes(2024, 4, 27, 24.8607, 67.0011, 5.0)

        val fajrStr  = result.fajr?.toTimeString()  ?: ""
        val dhuhrStr = result.dhuhr?.toTimeString() ?: ""

        assertTrue(fajrStr.contains(":"))
        assertTrue(dhuhrStr.contains(":"))

        // Times should be sequential
        assertTrue(result.sunrise!! > result.fajr!!)
        assertTrue(result.dhuhr!!  > result.sunrise!!)
        assertTrue(result.asr!!    > result.dhuhr!!)

        // Test Qibla
        val qibla = bearingToMecca(24.8607, 67.0011)
        assertNotNull(qibla)
        assertTrue(qibla!! > 250.0 && qibla < 270.0) // Karachi → Makkah ≈ 261°
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Timezone: UTC default behaviour
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `timezone defaults to UTC when omitted`() {
        val tauqeet = Tauqeet(method = CalculationMethod.KARACHI)

        // Call WITHOUT a timezoneOffset — should return UTC times
        val utcResult  = tauqeet.computePrayerTimes(2026, 8, 1, 24.8607, 67.0011)
        // Call WITH explicit 0.0 — should be identical
        val zeroResult = tauqeet.computePrayerTimes(2026, 8, 1, 24.8607, 67.0011, 0.0)

        assertEquals(utcResult.fajr,    zeroResult.fajr,    "UTC default must equal explicit 0.0 offset")
        assertEquals(utcResult.dhuhr,   zeroResult.dhuhr,   "UTC default must equal explicit 0.0 offset")
        assertEquals(utcResult.maghrib, zeroResult.maghrib, "UTC default must equal explicit 0.0 offset")
    }

    @Test
    fun `timezone offset shifts times by the correct milliseconds`() {
        val tauqeet = Tauqeet(method = CalculationMethod.KARACHI)

        val utcResult = tauqeet.computePrayerTimes(2026, 8, 1, 24.8607, 67.0011, 0.0)
        val pktResult = tauqeet.computePrayerTimes(2026, 8, 1, 24.8607, 67.0011, 5.0)

        // PKT is UTC+5 = 5 * 3_600_000 ms shift
        val shiftMs = 5 * 3_600_000L
        val msPerDay = 86_400_000L

        // Because of the modulo wrap the shift might push into the next "day bucket",
        // so we compare modulo msPerDay.
        val expectedDhuhr = ((utcResult.dhuhr!! + shiftMs) % msPerDay)
        assertEquals(expectedDhuhr, pktResult.dhuhr, "PKT dhuhr must be UTC dhuhr + 5 h")
    }

    @Test
    fun `DateComponents overload uses same timezone default`() {
        val tauqeet = Tauqeet(method = CalculationMethod.KARACHI)
        val date = DateComponents(2026, 8, 1)

        val fromInts   = tauqeet.computePrayerTimes(2026, 8, 1, 24.8607, 67.0011)
        val fromComponents = tauqeet.computePrayerTimes(date, 24.8607, 67.0011)

        assertEquals(fromInts.fajr,  fromComponents.fajr,  "DateComponents overload must match int overload")
        assertEquals(fromInts.dhuhr, fromComponents.dhuhr, "DateComponents overload must match int overload")
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Latitude validation
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `latitude exactly at -90 and 90 is valid`() {
        val tauqeet = Tauqeet()
        // Should NOT throw
        tauqeet.computePrayerTimes(2026, 6, 1, -90.0, 0.0)
        tauqeet.computePrayerTimes(2026, 6, 1,  90.0, 0.0)
    }

    @Test
    fun `latitude below -90 throws TauqeetException`() {
        val tauqeet = Tauqeet()
        assertFailsWith<TauqeetException> {
            tauqeet.computePrayerTimes(2026, 1, 1, -90.001, 0.0)
        }
    }

    @Test
    fun `latitude above 90 throws TauqeetException`() {
        val tauqeet = Tauqeet()
        assertFailsWith<TauqeetException> {
            tauqeet.computePrayerTimes(2026, 1, 1, 91.0, 0.0)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Longitude validation
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `longitude exactly at -180 and 180 is valid`() {
        val tauqeet = Tauqeet()
        tauqeet.computePrayerTimes(2026, 6, 1, 0.0, -180.0)
        tauqeet.computePrayerTimes(2026, 6, 1, 0.0,  180.0)
    }

    @Test
    fun `longitude below -180 throws TauqeetException`() {
        val tauqeet = Tauqeet()
        assertFailsWith<TauqeetException> {
            tauqeet.computePrayerTimes(2026, 1, 1, 0.0, -181.0)
        }
    }

    @Test
    fun `longitude above 180 throws TauqeetException`() {
        val tauqeet = Tauqeet()
        assertFailsWith<TauqeetException> {
            tauqeet.computePrayerTimes(2026, 1, 1, 0.0, 181.0)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Timezone offset validation
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `extreme valid timezone offsets -12 and +14 are accepted`() {
        val tauqeet = Tauqeet()
        tauqeet.computePrayerTimes(2026, 6, 1, 0.0, 0.0, -12.0)
        tauqeet.computePrayerTimes(2026, 6, 1, 0.0, 0.0,  14.0)
    }

    @Test
    fun `timezone offset below -12 throws TauqeetException`() {
        val tauqeet = Tauqeet()
        assertFailsWith<TauqeetException> {
            tauqeet.computePrayerTimes(2026, 1, 1, 0.0, 0.0, -13.0)
        }
    }

    @Test
    fun `timezone offset above 14 throws TauqeetException`() {
        val tauqeet = Tauqeet()
        assertFailsWith<TauqeetException> {
            tauqeet.computePrayerTimes(2026, 1, 1, 0.0, 0.0, 15.0)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Date validation
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `invalid month throws TauqeetException`() {
        val tauqeet = Tauqeet()
        assertFailsWith<TauqeetException> {
            tauqeet.computePrayerTimes(2026, 13, 1, 0.0, 0.0)
        }
        assertFailsWith<TauqeetException> {
            tauqeet.computePrayerTimes(2026, 0, 1, 0.0, 0.0)
        }
    }

    @Test
    fun `day 29 in February 2025 (non-leap) throws TauqeetException`() {
        val tauqeet = Tauqeet()
        assertFailsWith<TauqeetException> {
            tauqeet.computePrayerTimes(2025, 2, 29, 0.0, 0.0) // 2025 is not a leap year
        }
    }

    @Test
    fun `day 29 in February 2024 (leap year) is valid`() {
        val tauqeet = Tauqeet()
        // Should NOT throw
        tauqeet.computePrayerTimes(2024, 2, 29, 0.0, 0.0)
    }

    @Test
    fun `day 31 in a 30-day month throws TauqeetException`() {
        val tauqeet = Tauqeet()
        assertFailsWith<TauqeetException> {
            tauqeet.computePrayerTimes(2026, 4, 31, 0.0, 0.0) // April has 30 days
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Constructor parameter validation
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `negative elevation beyond -500 throws TauqeetException`() {
        assertFailsWith<TauqeetException> {
            Tauqeet(elevationMeters = -600.0)
        }
    }

    @Test
    fun `elevation above 9000 throws TauqeetException`() {
        assertFailsWith<TauqeetException> {
            Tauqeet(elevationMeters = 9001.0)
        }
    }

    @Test
    fun `temperature below -90 throws TauqeetException`() {
        assertFailsWith<TauqeetException> {
            Tauqeet(temperatureC = -91.0)
        }
    }

    @Test
    fun `pressure below 100 mbar throws TauqeetException`() {
        assertFailsWith<TauqeetException> {
            Tauqeet(pressureMbar = 50.0)
        }
    }

    @Test
    fun `pressure above 1100 mbar throws TauqeetException`() {
        assertFailsWith<TauqeetException> {
            Tauqeet(pressureMbar = 1200.0)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Qibla validation
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `qiblaDirection throws TauqeetException for invalid latitude`() {
        val tauqeet = Tauqeet()
        assertFailsWith<TauqeetException> {
            tauqeet.qiblaDirection(95.0, 0.0)
        }
    }

    @Test
    fun `qiblaDirection returns null at Kaaba coordinates`() {
        val tauqeet = Tauqeet()
        val result = tauqeet.qiblaDirection(21.422487, 39.826206)
        assertNull(result, "At the Kaaba, qiblaDirection must return null")
    }

    @Test
    fun `qiblaBearing throws TauqeetException for invalid longitude`() {
        val tauqeet = Tauqeet()
        assertFailsWith<TauqeetException> {
            tauqeet.qiblaBearing(0.0, 200.0)
        }
    }
}
