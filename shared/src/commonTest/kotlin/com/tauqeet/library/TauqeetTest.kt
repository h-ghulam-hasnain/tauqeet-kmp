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
    fun timezoneDefaultsToUtcWhenOmitted() {
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
    fun timezoneOffsetShiftsTimesByTheCorrectMilliseconds() {
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
    fun DateComponentsOverloadUsesSameTimezoneDefault() {
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
    fun latitudeExactlyAt90And90IsValid() {
        val tauqeet = Tauqeet()
        // Should NOT throw
        tauqeet.computePrayerTimes(2026, 6, 1, -90.0, 0.0)
        tauqeet.computePrayerTimes(2026, 6, 1,  90.0, 0.0)
    }

    @Test
    fun latitudeBelow90ThrowsTauqeetexception() {
        val tauqeet = Tauqeet()
        assertFailsWith<TauqeetException> {
            tauqeet.computePrayerTimes(2026, 1, 1, -90.001, 0.0)
        }
    }

    @Test
    fun latitudeAbove90ThrowsTauqeetexception() {
        val tauqeet = Tauqeet()
        assertFailsWith<TauqeetException> {
            tauqeet.computePrayerTimes(2026, 1, 1, 91.0, 0.0)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Longitude validation
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun longitudeExactlyAt180And180IsValid() {
        val tauqeet = Tauqeet()
        tauqeet.computePrayerTimes(2026, 6, 1, 0.0, -180.0)
        tauqeet.computePrayerTimes(2026, 6, 1, 0.0,  180.0)
    }

    @Test
    fun longitudeBelow180ThrowsTauqeetexception() {
        val tauqeet = Tauqeet()
        assertFailsWith<TauqeetException> {
            tauqeet.computePrayerTimes(2026, 1, 1, 0.0, -181.0)
        }
    }

    @Test
    fun longitudeAbove180ThrowsTauqeetexception() {
        val tauqeet = Tauqeet()
        assertFailsWith<TauqeetException> {
            tauqeet.computePrayerTimes(2026, 1, 1, 0.0, 181.0)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Timezone offset validation
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun extremeValidTimezoneOffsets12And14AreAccepted() {
        val tauqeet = Tauqeet()
        tauqeet.computePrayerTimes(2026, 6, 1, 0.0, 0.0, -12.0)
        tauqeet.computePrayerTimes(2026, 6, 1, 0.0, 0.0,  14.0)
    }

    @Test
    fun timezoneOffsetBelow12ThrowsTauqeetexception() {
        val tauqeet = Tauqeet()
        assertFailsWith<TauqeetException> {
            tauqeet.computePrayerTimes(2026, 1, 1, 0.0, 0.0, -13.0)
        }
    }

    @Test
    fun timezoneOffsetAbove14ThrowsTauqeetexception() {
        val tauqeet = Tauqeet()
        assertFailsWith<TauqeetException> {
            tauqeet.computePrayerTimes(2026, 1, 1, 0.0, 0.0, 15.0)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Date validation
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun invalidMonthThrowsTauqeetexception() {
        val tauqeet = Tauqeet()
        assertFailsWith<TauqeetException> {
            tauqeet.computePrayerTimes(2026, 13, 1, 0.0, 0.0)
        }
        assertFailsWith<TauqeetException> {
            tauqeet.computePrayerTimes(2026, 0, 1, 0.0, 0.0)
        }
    }

    @Test
    fun day29InFebruary2025nonleapThrowsTauqeetexception() {
        val tauqeet = Tauqeet()
        assertFailsWith<TauqeetException> {
            tauqeet.computePrayerTimes(2025, 2, 29, 0.0, 0.0) // 2025 is not a leap year
        }
    }

    @Test
    fun day29InFebruary2024leapYearIsValid() {
        val tauqeet = Tauqeet()
        // Should NOT throw
        tauqeet.computePrayerTimes(2024, 2, 29, 0.0, 0.0)
    }

    @Test
    fun day31InA30dayMonthThrowsTauqeetexception() {
        val tauqeet = Tauqeet()
        assertFailsWith<TauqeetException> {
            tauqeet.computePrayerTimes(2026, 4, 31, 0.0, 0.0) // April has 30 days
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Constructor parameter validation
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun negativeElevationBeyond500ThrowsTauqeetexception() {
        assertFailsWith<TauqeetException> {
            Tauqeet(elevationMeters = -600.0)
        }
    }

    @Test
    fun elevationAbove9000ThrowsTauqeetexception() {
        assertFailsWith<TauqeetException> {
            Tauqeet(elevationMeters = 9001.0)
        }
    }

    @Test
    fun temperatureBelow90ThrowsTauqeetexception() {
        assertFailsWith<TauqeetException> {
            Tauqeet(temperatureC = -91.0)
        }
    }

    @Test
    fun pressureBelow100MbarThrowsTauqeetexception() {
        assertFailsWith<TauqeetException> {
            Tauqeet(pressureMbar = 50.0)
        }
    }

    @Test
    fun pressureAbove1100MbarThrowsTauqeetexception() {
        assertFailsWith<TauqeetException> {
            Tauqeet(pressureMbar = 1200.0)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Qibla validation
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun qiblaDirectionThrowsTauqeetexceptionForInvalidLatitude() {
        val tauqeet = Tauqeet()
        assertFailsWith<TauqeetException> {
            tauqeet.qiblaDirection(95.0, 0.0)
        }
    }

    @Test
    fun qiblaDirectionReturnsNullAtKaabaCoordinates() {
        val tauqeet = Tauqeet()
        val result = tauqeet.qiblaDirection(21.422487, 39.826206)
        assertNull(result, "At the Kaaba, qiblaDirection must return null")
    }

    @Test
    fun qiblaBearingThrowsTauqeetexceptionForInvalidLongitude() {
        val tauqeet = Tauqeet()
        assertFailsWith<TauqeetException> {
            tauqeet.qiblaBearing(0.0, 200.0)
        }
    }
}
