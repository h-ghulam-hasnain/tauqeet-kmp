package com.tauqeet.library

/**
 * Thrown when invalid input parameters are passed to any Tauqeet API method.
 *
 * All validation happens at the boundary of the public API (in [Tauqeet]) before
 * any internal computation begins, so a [TauqeetException] always indicates a
 * caller-side mistake rather than an internal engine failure.
 *
 * @param message A human-readable description of what was wrong and what the valid range is.
 */
class TauqeetException(message: String) : IllegalArgumentException(message)

// ─────────────────────────────────────────────────────────────────────────────
// Internal validation helpers
// ─────────────────────────────────────────────────────────────────────────────

internal fun validateLatitude(lat: Double) {
    if (lat.isNaN() || lat < -90.0 || lat > 90.0) {
        throw TauqeetException(
            "Invalid latitude: $lat. Must be in the range [-90.0, 90.0]."
        )
    }
}

internal fun validateLongitude(lng: Double) {
    if (lng.isNaN() || lng < -180.0 || lng > 180.0) {
        throw TauqeetException(
            "Invalid longitude: $lng. Must be in the range [-180.0, 180.0]."
        )
    }
}

internal fun validateTimezoneOffset(offset: Double) {
    // UTC offsets in practice range from -12:00 to +14:00
    if (offset.isNaN() || offset < -12.0 || offset > 14.0) {
        throw TauqeetException(
            "Invalid timezoneOffset: $offset. Must be in the range [-12.0, 14.0] hours from UTC."
        )
    }
}

internal fun validateDate(year: Int, month: Int, day: Int) {
    if (year < 1 || year > 3000) {
        throw TauqeetException("Invalid year: $year. Must be in the range [1, 3000] due to astronomical limits.")
    }
    if (month < 1 || month > 12) {
        throw TauqeetException("Invalid month: $month. Must be in the range [1, 12].")
    }
    val maxDay = daysInMonth(year, month)
    if (day < 1 || day > maxDay) {
        throw TauqeetException(
            "Invalid day: $day for month $month/$year. Must be in the range [1, $maxDay]."
        )
    }
}

internal fun validateElevation(meters: Double) {
    // Dead Sea is ~-430m; Mount Everest is ~8848m. We allow a generous range.
    if (meters.isNaN() || meters < -500.0 || meters > 9000.0) {
        throw TauqeetException(
            "Invalid elevationMeters: $meters. Must be in the range [-500.0, 9000.0] meters."
        )
    }
}

internal fun validateTemperature(tempC: Double) {
    // Absolute zero is -273.15°C. Hottest recorded surface temp ~80°C.
    if (tempC.isNaN() || tempC < -90.0 || tempC > 80.0) {
        throw TauqeetException(
            "Invalid temperatureC: $tempC. Must be in the range [-90.0, 80.0] °C."
        )
    }
}

internal fun validatePressure(mbar: Double) {
    // Everest summit ~300 mbar; standard sea level ~1013 mbar; max recorded ~1085 mbar.
    if (mbar.isNaN() || mbar < 100.0 || mbar > 1100.0) {
        throw TauqeetException(
            "Invalid pressureMbar: $mbar. Must be in the range [100.0, 1100.0] mbar."
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Calendar helper — leap-year-aware days-in-month
// ─────────────────────────────────────────────────────────────────────────────

private fun isLeapYear(year: Int): Boolean {
    val isGregorian = year >= 1582
    return if (isGregorian) {
        (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    } else {
        year % 4 == 0
    }
}

private fun daysInMonth(year: Int, month: Int): Int = when (month) {
    1, 3, 5, 7, 8, 10, 12 -> 31
    4, 6, 9, 11             -> 30
    2                       -> if (isLeapYear(year)) 29 else 28
    else                    -> 31 // unreachable after month validation
}
