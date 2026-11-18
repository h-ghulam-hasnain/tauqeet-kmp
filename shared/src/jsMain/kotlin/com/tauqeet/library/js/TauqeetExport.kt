@file:JsExport
@file:Suppress("NON_EXPORTABLE_TYPE")

package com.tauqeet.library

import kotlinx.coroutines.GlobalScope
import kotlin.js.Promise
import kotlinx.datetime.Instant
import kotlinx.coroutines.promise

/**
 * Lightweight DTO that maps cleanly to a plain JavaScript object for the Promise-based API.
 *
 * All time fields are raw millisecond-since-midnight strings as returned by the Kotlin engine.
 * Use [longToString] to safely convert [Long] values to strings without JS precision loss.
 *
 * @property fajr     Fajr time as a millisecond string, or empty string if unavailable.
 * @property sunrise  Sunrise time as a millisecond string, or empty string if unavailable.
 * @property dhuhr    Dhuhr time as a millisecond string, or empty string if unavailable.
 * @property asr      Asr time as a millisecond string, or empty string if unavailable.
 * @property maghrib  Maghrib time as a millisecond string, or empty string if unavailable.
 * @property isha     Isha time as a millisecond string, or empty string if unavailable.
 */
@JsExport
data class PrayerTimes(
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String
)

/**
 * Computes Islamic prayer times and returns them as a JavaScript [Promise].
 *
 * Parses [isoDate] as a UTC midnight Instant, runs the VSOP87 engine,
 * and resolves with a [PrayerTimes] DTO. All times are in UTC (timezone offset = 0.0).
 *
 * This is the **asynchronous (Promise-returning)** API. For a synchronous variant with
 * full configuration options (method, madhab, timezone), see [calculatePrayerTimesKmp]
 * or the [com.tauqeet.library.js.TauqeetJsBridge] object.
 *
 * JavaScript usage:
 * ```javascript
 * // const times = await calculatePrayerTimes(24.45, 54.37, "2026-01-15", 3);
 * // console.log(times.fajr); // millisecond string
 * ```
 *
 * @param latitude   Observer's latitude in decimal degrees (-90.0 to 90.0).
 * @param longitude  Observer's longitude in decimal degrees (-180.0 to 180.0).
 * @param isoDate    Date in ISO 8601 format YYYY-MM-DD (e.g. "2026-01-15"), parsed as UTC midnight.
 * @param method     Integer index selecting the calculation method (default 2). Currently reserved;
 *                   the internal engine uses KARACHI parameters.
 * @return A [Promise] that resolves to a [PrayerTimes] DTO with all six prayer times as strings.
 */
@JsExport
fun calculatePrayerTimes(
    latitude: Double,
    longitude: Double,
    isoDate: String, // e.g. "2026-11-04"
    method: Int = 2
): Promise<PrayerTimes> = GlobalScope.promise {
    // Parse ISO date to Instant (UTC midnight)
    val instant = Instant.parse(isoDate)
    // Internal library call (still suspend)
    val internal = Tauqeet().computePrayerTimes(instant, latitude, longitude, timezoneOffset = 0.0, includeAdvancedMetadata = false)
    // Map to JS‑friendly DTO
    PrayerTimes(
        fajr = internal.fajr?.toString() ?: "",
        sunrise = internal.sunrise?.toString() ?: "",
        dhuhr = internal.dhuhr?.toString() ?: "",
        asr = internal.asr?.toString() ?: "",
        maghrib = internal.maghrib?.toString() ?: "",
        isha = internal.isha?.toString() ?: ""
    )
}

/**
 * Returns the Qibla bearing from the given location to the Ka'bah in Makkah.
 *
 * JavaScript usage:
 * ```javascript
 * // const bearing = qiblaBearing(24.45, 54.37); // e.g. 258.4 degrees clockwise from north
 * ```
 *
 * @param lat  Observer's latitude in decimal degrees (-90.0 to 90.0).
 * @param lng  Observer's longitude in decimal degrees (-180.0 to 180.0).
 * @return Bearing in decimal degrees (0.0 to 360.0, clockwise from north),
 *         or `null` if the observer's location coincides with the Ka'bah.
 */
@JsExport
fun qiblaBearing(lat: Double, lng: Double): Double? = Tauqeet().qiblaBearing(lat, lng)

/**
 * Converts a [Long] millisecond timestamp to its exact [String] representation.
 *
 * JavaScript's `number` type is a 64-bit IEEE 754 float with only 53 bits of integer
 * mantissa, so Long values larger than 2^53 lose precision when passed directly
 * to JavaScript. This helper converts on the Kotlin side before crossing the
 * JS boundary, guaranteeing exact decimal representation.
 *
 * @param value  The [Long] value to convert.
 * @return The exact decimal string representation of [value].
 */
@JsExport
fun longToString(value: Long): String = value.toString()
