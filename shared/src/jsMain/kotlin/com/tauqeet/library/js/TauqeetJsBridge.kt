@file:JsExport

package com.tauqeet.library.js

import com.tauqeet.library.DateComponents
import com.tauqeet.library.Tauqeet
import com.tauqeet.library.prayers.CalculationMethod
import com.tauqeet.library.prayers.HighLatitudeRule
import com.tauqeet.library.prayers.Madhab
import com.tauqeet.library.prayers.prayerRequest
import kotlin.js.JsExport

/**
 * Result object returned by [TauqeetJsBridge.calculatePrayerTimes] and [calculatePrayerTimesKmp].
 *
 * All time fields are milliseconds since UTC midnight, expressed as [Double] to remain
 * representable in JavaScript without precision loss. `null` indicates the event could not
 * be computed (e.g., polar night or continuous twilight).
 *
 * @property fajr              Fajr time in milliseconds, or `null`.
 * @property sunrise           Sunrise time in milliseconds, or `null`.
 * @property dhahwaKubra       Dhahwa Kubra (midpoint of Fajr-Sunset) in milliseconds, or `null`.
 * @property dhuhr             Dhuhr time in milliseconds, or `null` during polar night.
 * @property asr               Asr time in milliseconds, or `null`.
 * @property maghrib           Maghrib time in milliseconds, or `null`.
 * @property isha              Isha time in milliseconds, or `null`.
 * @property status            Computation status name (e.g. `"SUCCESS"`, `"POLAR_NIGHT"`, `"ERROR"`).
 * @property method            The calculation method name used (e.g. `"KARACHI"`).
 * @property madhab            The madhab name used (e.g. `"HANAFI"`).
 * @property highLatitudeRule  The high-latitude rule name used (e.g. `"MIDDLE_OF_NIGHT"`).
 * @property errorMessage      Human-readable error description, or `null` on success.
 */
@JsExport
class PrayerTimesJsResult(
    val fajr: Double?,
    val sunrise: Double?,
    val dhahwaKubra: Double?,
    val dhuhr: Double?,
    val asr: Double?,
    val maghrib: Double?,
    val isha: Double?,
    val status: String?,
    val method: String?,
    val madhab: String?,
    val highLatitudeRule: String?,
    val errorMessage: String? = null
)

/**
 * Synchronous JS API surface for computing Islamic prayer times and Qibla direction.
 *
 * Accepts string-based method/madhab/highLatitudeRule names (case-insensitive) and returns
 * a [PrayerTimesJsResult] directly — no Promise, no await required.
 *
 * For the **asynchronous (Promise-returning)** variant, see `calculatePrayerTimes`
 * in `TauqeetExport.kt`.
 */
@JsExport
object TauqeetJsBridge {
    /**
     * Computes Islamic prayer times synchronously for the given date, location, and method.
     *
     * String parameters are matched case-insensitively against the enum entry names;
     * unrecognised values fall back to the defaults shown below.
     *
     * This is the **synchronous** API. For a Promise-based variant, see
     * `calculatePrayerTimes` in `TauqeetExport.kt`.
     *
     * JavaScript usage:
     * ```javascript
     * // const times = TauqeetJsBridge.calculatePrayerTimes(
     * //   24.45, 54.37, 2026, 1, 15, "MWL", "SHAFI", "MIDDLE_OF_NIGHT", 4.0
     * // );
     * ```
     *
     * @param lat               Observer's latitude in decimal degrees (-90.0 to 90.0).
     * @param lng               Observer's longitude in decimal degrees (-180.0 to 180.0).
     * @param year              Gregorian year.
     * @param month             Gregorian month (1-12).
     * @param day               Day of the month (1-31).
     * @param method            Calculation method name. Default: `"KARACHI"`.
     * @param madhab            Madhab name. Default: `"HANAFI"`.
     * @param highLatitudeRule  High-latitude rule name. Default: `"MIDDLE_OF_NIGHT"`.
     * @param timezoneOffset    Hours offset from UTC (-12.0 to +14.0). Default: `0.0`.
     * @return [PrayerTimesJsResult] with all seven prayer times and metadata,
     *         or an error result with `status = "ERROR"` and a non-null [PrayerTimesJsResult.errorMessage].
     */
    fun calculatePrayerTimes(
        lat: Double,
        lng: Double,
        year: Int,
        month: Int,
        day: Int,
        method: String = "KARACHI",
        madhab: String = "HANAFI",
        highLatitudeRule: String = "MIDDLE_OF_NIGHT",
        timezoneOffset: Double = 0.0
    ): PrayerTimesJsResult {
        return calculatePrayerTimesKmp(
            lat = lat,
            lng = lng,
            year = year,
            month = month,
            day = day,
            method = method,
            madhab = madhab,
            highLatitudeRule = highLatitudeRule,
            timezoneOffset = timezoneOffset
        )
    }
}

/**
 * Top-level implementation of the synchronous prayer-time calculation, shared by
 * [TauqeetJsBridge.calculatePrayerTimes] and callable directly from JavaScript.
 *
 * Resolves string method/madhab/highLatitudeRule names to their enum counterparts
 * (case-insensitive, falls back to defaults on no match), then delegates to the
 * [com.tauqeet.library.Tauqeet] engine via the [com.tauqeet.library.prayers.prayerRequest] DSL.
 * All exceptions are caught and returned as an error [PrayerTimesJsResult].
 *
 * Prefer [TauqeetJsBridge.calculatePrayerTimes] from JavaScript for a stable, object-namespaced
 * entry point. This top-level function is exported as a secondary convenience.
 *
 * JavaScript usage:
 * ```javascript
 * // const times = calculatePrayerTimesKmp(24.45, 54.37, 2026, 1, 15, "MWL", "SHAFI", "MIDDLE_OF_NIGHT", 4.0);
 * ```
 *
 * @param lat               Observer's latitude in decimal degrees (-90.0 to 90.0).
 * @param lng               Observer's longitude in decimal degrees (-180.0 to 180.0).
 * @param year              Gregorian year.
 * @param month             Gregorian month (1-12).
 * @param day               Day of the month (1-31).
 * @param method            Calculation method name. Default: `"KARACHI"`.
 * @param madhab            Madhab name. Default: `"HANAFI"`.
 * @param highLatitudeRule  High-latitude rule name. Default: `"MIDDLE_OF_NIGHT"`.
 * @param timezoneOffset    Hours offset from UTC (-12.0 to +14.0). Default: `0.0`.
 * @return [PrayerTimesJsResult] with all seven prayer times and metadata,
 *         or an error result with `status = "ERROR"` on any exception.
 */
@JsExport
fun calculatePrayerTimesKmp(
    lat: Double,
    lng: Double,
    year: Int,
    month: Int,
    day: Int,
    method: String = "KARACHI",
    madhab: String = "HANAFI",
    highLatitudeRule: String = "MIDDLE_OF_NIGHT",
    timezoneOffset: Double = 0.0
): PrayerTimesJsResult {
    return try {
        val calcMethod = CalculationMethod.entries.find { it.name.equals(method, ignoreCase = true) } ?: CalculationMethod.KARACHI
        val calcMadhab = Madhab.entries.find { it.name.equals(madhab, ignoreCase = true) } ?: Madhab.HANAFI
        val calcHighLatRule = HighLatitudeRule.entries.find { it.name.equals(highLatitudeRule, ignoreCase = true) } ?: HighLatitudeRule.MIDDLE_OF_NIGHT

        val result = Tauqeet(
            method = calcMethod,
            madhab = calcMadhab,
            highLatitudeRule = calcHighLatRule
        ).computePrayerTimes(
            prayerRequest {
                latitude = lat
                longitude = lng
                date = DateComponents(year, month, day)
                timeZoneOffset = timezoneOffset
                calculation {
                    this.method = calcMethod
                    this.madhab = calcMadhab
                    this.highLatitudeRule = calcHighLatRule
                }
            }
        )

        PrayerTimesJsResult(
            fajr = result.fajr?.toDouble(),
            sunrise = result.sunrise?.toDouble(),
            dhahwaKubra = result.dhahwaKubra?.toDouble(),
            dhuhr = result.dhuhr?.toDouble(),
            asr = result.asr?.toDouble(),
            maghrib = result.maghrib?.toDouble(),
            isha = result.isha?.toDouble(),
            status = result.status.name,
            method = result.metadata?.method ?: calcMethod.name,
            madhab = result.metadata?.madhab ?: calcMadhab.name,
            highLatitudeRule = result.metadata?.highLatitudeRule ?: calcHighLatRule.name,
            errorMessage = null
        )
    } catch (e: Throwable) {
        PrayerTimesJsResult(
            fajr = null,
            sunrise = null,
            dhahwaKubra = null,
            dhuhr = null,
            asr = null,
            maghrib = null,
            isha = null,
            status = "ERROR",
            method = method,
            madhab = madhab,
            highLatitudeRule = highLatitudeRule,
            errorMessage = e.message
        )
    }
}
