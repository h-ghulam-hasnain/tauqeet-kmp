package com.tauqeet.library

import com.tauqeet.library.prayers.CalculationMethod
import com.tauqeet.library.prayers.HighLatitudeRule
import com.tauqeet.library.prayers.Madhab
import com.tauqeet.library.prayers.PrayerTimesResult
import com.tauqeet.library.prayers.computePrayerTimes as internalComputePrayerTimes
import com.tauqeet.library.qibla.bearingToMecca
import com.tauqeet.library.qibla.tauqeetQibla
import com.tauqeet.library.qibla.QiblaResult
import com.tauqeet.library.time.dateToJulianDay

data class DateComponents(val year: Int, val month: Int, val day: Int)

class Tauqeet(
    val method: CalculationMethod = CalculationMethod.KARACHI,
    val madhab: Madhab = Madhab.HANAFI,
    val highLatitudeRule: HighLatitudeRule = HighLatitudeRule.MIDDLE_OF_NIGHT,
    val elevationMeters: Double = 0.0,
    val temperatureC: Double = 12.714,
    val pressureMbar: Double = 1010.0,
    val customMethodParams: com.tauqeet.library.prayers.CalculationMethodParameters? = null
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
        timezoneOffset: Double = 0.0,
        includeAdvancedMetadata: Boolean = false
    ): PrayerTimesResult {
        // Use standard fractional day (0.0 corresponds to 00:00 UTC)
        val jd = dateToJulianDay(year, month, day.toDouble())
        val paramsToUse = customMethodParams ?: method.params
        val methodName = if (customMethodParams != null) "CUSTOM" else method.name
        val rawResult = internalComputePrayerTimes(lat, lng, jd, paramsToUse, methodName, madhab, highLatitudeRule, elevationMeters, temperatureC, pressureMbar, includeAdvancedMetadata)

        // The internal engine returns times in UTC milliseconds since midnight.
        // We add timezoneOffset * 3600000 to get local milliseconds.
        val tzOffsetMs = (timezoneOffset * 3600000.0).toLong()
        
        val msPerDay = 86400000L
        return PrayerTimesResult(
            fajr = rawResult.fajr?.let { (it + tzOffsetMs + msPerDay) % msPerDay },
            sunrise = rawResult.sunrise?.let { (it + tzOffsetMs + msPerDay) % msPerDay },
            dhahwaKubra = rawResult.dhahwaKubra?.let { (it + tzOffsetMs + msPerDay) % msPerDay },
            dhuhr = rawResult.dhuhr?.let { (it + tzOffsetMs + msPerDay) % msPerDay },
            asr = rawResult.asr?.let { (it + tzOffsetMs + msPerDay) % msPerDay },
            maghrib = rawResult.maghrib?.let { (it + tzOffsetMs + msPerDay) % msPerDay },
            isha = rawResult.isha?.let { (it + tzOffsetMs + msPerDay) % msPerDay },
            metadata = rawResult.metadata,
            astronomicalMetadata = rawResult.astronomicalMetadata
        )
    }

    /**
     * Convenience method to compute the prayer times using a DateComponents object.
     */
    fun computePrayerTimes(
        date: DateComponents,
        lat: Double,
        lng: Double,
        timezoneOffset: Double = 0.0,
        includeAdvancedMetadata: Boolean = false
    ): PrayerTimesResult {
        return computePrayerTimes(date.year, date.month, date.day, lat, lng, timezoneOffset, includeAdvancedMetadata)
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

/**
 * Top-level public function to compute the Qibla direction and distance.
 * @param lat Observer's latitude
 * @param lng Observer's longitude
 * @return A QiblaResult containing bearing and distance, or null if coincident to Mecca.
 */
fun qiblaDirection(lat: Double, lng: Double): QiblaResult? {
    return tauqeetQibla(lat, lng)
}
