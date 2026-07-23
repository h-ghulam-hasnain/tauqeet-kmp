package com.tauqeet.library

import com.tauqeet.library.prayers.CalculationMethod
import com.tauqeet.library.prayers.HighLatitudeRule
import com.tauqeet.library.prayers.Madhab
import com.tauqeet.library.prayers.PrayerTimesResult
import com.tauqeet.library.prayers.computePrayerTimes as internalComputePrayerTimes
import com.tauqeet.library.qibla.bearingToMecca
import com.tauqeet.library.time.dateToJulianDay

data class DateComponents(val year: Int, val month: Int, val day: Int)

class Tauqeet(
    val method: CalculationMethod = CalculationMethod.MWL,
    val madhab: Madhab = Madhab.SHAFI,
    val highLatitudeRule: HighLatitudeRule = HighLatitudeRule.MIDDLE_OF_NIGHT
) {
    /**
     * Computes the prayer times for a given date and location.
     * 
     * @param year The year (e.g. 2024)
     * @param month The month (1-12)
     * @param day The day of the month (1-31)
     * @param lat Observer's latitude in degrees
     * @param lng Observer's longitude in degrees
     * @param timezoneOffset Timezone offset from UTC in hours (e.g., 5.0 for UTC+5). Defaults to 0.0 (UTC).
     */
    fun computePrayerTimes(
        year: Int,
        month: Int,
        day: Int,
        lat: Double,
        lng: Double,
        timezoneOffset: Double = 0.0
    ): PrayerTimesResult {
        // Use standard fractional day (0.0 corresponds to 00:00 UTC)
        val jd = dateToJulianDay(year, month, day.toDouble())
        val rawResult = internalComputePrayerTimes(lat, lng, jd, method, madhab, highLatitudeRule)

        // The internal engine returns times in UTC minutes since midnight.
        // We add timezoneOffset * 60 to get local minutes.
        val tzOffsetMinutes = timezoneOffset * 60.0
        
        return PrayerTimesResult(
            fajr = (rawResult.fajr + tzOffsetMinutes + 1440.0) % 1440.0,
            sunrise = (rawResult.sunrise + tzOffsetMinutes + 1440.0) % 1440.0,
            dhuhr = (rawResult.dhuhr + tzOffsetMinutes + 1440.0) % 1440.0,
            asr = (rawResult.asr + tzOffsetMinutes + 1440.0) % 1440.0,
            maghrib = (rawResult.maghrib + tzOffsetMinutes + 1440.0) % 1440.0,
            isha = (rawResult.isha + tzOffsetMinutes + 1440.0) % 1440.0
        )
    }

    /**
     * Convenience method to compute the prayer times using a DateComponents object.
     */
    fun computePrayerTimes(
        date: DateComponents,
        lat: Double,
        lng: Double,
        timezoneOffset: Double = 0.0
    ): PrayerTimesResult {
        return computePrayerTimes(date.year, date.month, date.day, lat, lng, timezoneOffset)
    }
}

/**
 * Top-level public function to compute the Qibla bearing from a given location.
 * @param lat Observer's latitude
 * @param lng Observer's longitude
 * @return The bearing in degrees from True North, or null if coincident/antipodal to Mecca.
 */
fun qiblaBearing(lat: Double, lng: Double): Double? {
    return bearingToMecca(lat, lng)
}
