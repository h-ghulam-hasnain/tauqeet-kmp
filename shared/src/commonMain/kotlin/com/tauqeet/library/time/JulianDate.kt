package com.tauqeet.library.time

import kotlin.math.floor
import kotlin.math.round
import kotlin.math.truncate

class JulianDateComponents(
    val year: Int,
    val month: Int,
    val day: Double
)

class TimeArgument(
    val jd: Double,
    val jde: Double,
    val t: Double,
    val te: Double,
    val tau: Double
)

class TimeParts(
    val hour: Int,
    val minute: Int,
    val second: Int
)

/**
 * Converts a standard Gregorian calendar date to a Julian Day number.
 */
fun dateToJulianDay(year: Int, month: Int, day: Double): Double {
    var y = year
    var m = month
    if (m <= 2) {
        y -= 1
        m += 12
    }

    val a = truncate(y / 100.0)
    // Julian vs Gregorian transition logic: Gregorian calendar started Oct 15, 1582.
    val isGregorian = year > 1582 || (year == 1582 && (month > 10 || (month == 10 && day >= 15.0)))
    val b = if (isGregorian) {
        2.0 - a + truncate(a / 4.0)
    } else {
        0.0
    }
    
    return truncate(365.25 * (y + 4716.0)) + truncate(30.6001 * (m + 1.0)) + day + b - 1524.5
}

/**
 * Converts a Julian Day number back into its Gregorian calendar components.
 */
fun julianDayToDate(jd: Double): JulianDateComponents {
    val z = truncate(jd + 0.5)
    val f = jd + 0.5 - z
    val a = if (z < 2299161.0) {
        z
    } else {
        val alpha = truncate((z - 1867216.25) / 36524.25)
        z + 1.0 + alpha - truncate(alpha / 4.0)
    }
    val b = a + 1524.0
    val c = truncate((b - 122.1) / 365.25)
    val d = truncate(365.25 * c)
    val e = truncate((b - d) / 30.6001)
    val day = b - d - truncate(30.6001 * e) + f
    val month = if (e < 14.0) e - 1.0 else e - 13.0
    val year = if (month > 2.0) c - 4716.0 else c - 4715.0
    return JulianDateComponents(year.toInt(), month.toInt(), day)
}

/**
 * Computes standard time arguments used extensively in astronomical ephemeris equations.
 */
fun timeArguments(j: Double, ut: Double, deltaT: Double): TimeArgument {
    val jd = j + ut / 24.0
    val jde = jd + deltaT / 86400.0
    val t = (jd - 2451545.0) / 36525.0
    val te = (jde - 2451545.0) / 36525.0
    val tau = te / 10.0
    return TimeArgument(jd, jde, t, te, tau)
}

/**
 * Normalizes a Universal Time value to ensure it falls within 0 to 24 hours.
 */
fun normalizeTime(j: Double, ut: Double): Pair<Double, Double> {
    var resultJ = j
    var resultUt = ut
    while (resultUt < 0.0) {
        resultJ -= 1.0
        resultUt += 24.0
    }
    while (resultUt >= 24.0) {
        resultJ += 1.0
        resultUt -= 24.0
    }
    return Pair(resultJ, resultUt)
}

/**
 * Normalizes an angle representing a meridian or longitude to the range [-180, 180].
 */
fun normalizeMeridianAngle(angle: Double): Double {
    val normalized = angle % 360.0
    if (normalized > 180.0) {
        return normalized - 360.0
    }
    if (normalized <= -180.0) {
        return normalized + 360.0
    }
    return normalized
}

/**
 * Splits a fractional time value into its integer components.
 */
fun asTimeParts(value: Double): TimeParts {
    var hour = truncate(value).toInt()
    var minute = truncate(60.0 * (value - hour)).toInt()
    var second = round(3600.0 * (value - hour - minute / 60.0)).toInt()
    
    if (second >= 60) {
        minute += second / 60
        second %= 60
    }
    if (minute >= 60) {
        hour += minute / 60
        minute %= 60
    }
    
    return TimeParts(hour, minute, second)
}
