package com.tauqeet.library.time

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CalendarRegressionTest {

    @Test
    fun testDateToJulianDayAndBack() {
        // Test standard date
        val jd1 = dateToJulianDay(2026, 8, 14.5) // Noon Aug 14, 2026
        val date1 = julianDayToDate(jd1)
        assertEquals(2026, date1.year)
        assertEquals(8, date1.month)
        assertEquals(14.5, date1.day, 0.0001)

        // Test leap year (Feb 29)
        val jdLeap = dateToJulianDay(2024, 2, 29.0)
        val dateLeap = julianDayToDate(jdLeap)
        assertEquals(2024, dateLeap.year)
        assertEquals(2, dateLeap.month)
        assertEquals(29.0, dateLeap.day, 0.0001)

        // Test historical date (Gregorian cutover, Oct 15 1582)
        val jdHistorical = dateToJulianDay(1582, 10, 15.0)
        val dateHistorical = julianDayToDate(jdHistorical)
        assertEquals(1582, dateHistorical.year)
        assertEquals(10, dateHistorical.month)
        assertEquals(15.0, dateHistorical.day, 0.0001)
    }

    @Test
    fun testMonthTransitions() {
        // End of Jan to Feb
        val jdJan31 = dateToJulianDay(2026, 1, 31.0)
        val dateJan31 = julianDayToDate(jdJan31)
        assertEquals(2026, dateJan31.year)
        assertEquals(1, dateJan31.month)
        assertEquals(31.0, dateJan31.day, 0.0001)

        val jdFeb1 = jdJan31 + 1.0
        val dateFeb1 = julianDayToDate(jdFeb1)
        assertEquals(2026, dateFeb1.year)
        assertEquals(2, dateFeb1.month)
        assertEquals(1.0, dateFeb1.day, 0.0001)
        
        // Leap year transition
        val jdFeb29 = dateToJulianDay(2024, 2, 29.0)
        val jdMar1 = jdFeb29 + 1.0
        val dateMar1 = julianDayToDate(jdMar1)
        assertEquals(2024, dateMar1.year)
        assertEquals(3, dateMar1.month)
        assertEquals(1.0, dateMar1.day, 0.0001)
        
        // Non-leap year transition
        val jdFeb28_25 = dateToJulianDay(2025, 2, 28.0)
        val jdMar1_25 = jdFeb28_25 + 1.0
        val dateMar1_25 = julianDayToDate(jdMar1_25)
        assertEquals(2025, dateMar1_25.year)
        assertEquals(3, dateMar1_25.month)
        assertEquals(1.0, dateMar1_25.day, 0.0001)
    }

    @Test
    fun testYearBoundaries() {
        val jdDec31 = dateToJulianDay(2025, 12, 31.0)
        val jdJan1 = jdDec31 + 1.0
        val dateJan1 = julianDayToDate(jdJan1)
        assertEquals(2026, dateJan1.year)
        assertEquals(1, dateJan1.month)
        assertEquals(1.0, dateJan1.day, 0.0001)
    }

    @Test
    fun testNormalizeTime() {
        val (j1, ut1) = normalizeTime(2451545.0, 25.5)
        assertEquals(2451546.0, j1, 0.0001)
        assertEquals(1.5, ut1, 0.0001)

        val (j2, ut2) = normalizeTime(2451545.0, -1.5)
        assertEquals(2451544.0, j2, 0.0001)
        assertEquals(22.5, ut2, 0.0001)
    }

    @Test
    fun testNormalizeMeridianAngle() {
        assertEquals(0.0, normalizeMeridianAngle(360.0), 0.0001)
        assertEquals(-170.0, normalizeMeridianAngle(190.0), 0.0001)
        assertEquals(170.0, normalizeMeridianAngle(-190.0), 0.0001)
        assertEquals(90.0, normalizeMeridianAngle(450.0), 0.0001)
    }

    @Test
    fun testAsTimeParts() {
        val parts1 = asTimeParts(5.5)
        assertEquals(5, parts1.hour)
        assertEquals(30, parts1.minute)
        assertEquals(0, parts1.second)

        // Check rollover
        val parts2 = asTimeParts(5.9999)
        // 5.9999 hours is 5h 59m 59.64s -> rounds to 5h 60m 0s -> 6h 0m 0s
        assertEquals(6, parts2.hour)
        assertEquals(0, parts2.minute)
        assertEquals(0, parts2.second)
    }
}
