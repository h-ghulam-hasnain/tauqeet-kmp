package com.tauqeet.library

import com.tauqeet.library.prayers.CalculationMethod
import com.tauqeet.library.prayers.HighLatitudeRule
import com.tauqeet.library.prayers.Madhab
import com.tauqeet.library.prayers.PrayerRequest
import com.tauqeet.library.prayers.PrayerTimesResult
import com.tauqeet.library.prayers.ResolutionInfo
import com.tauqeet.library.prayers.SolverKind
import com.tauqeet.library.prayers.computePrayerTimes as internalComputePrayerTimes
import com.tauqeet.library.prayers.prayerRequest
import com.tauqeet.library.qibla.bearingToMecca
import com.tauqeet.library.qibla.tauqeetQibla
import com.tauqeet.library.qibla.QiblaResult
import com.tauqeet.library.time.dateToJulianDay

data class DateComponents(val year: Int, val month: Int, val day: Int)

/**
 * The main entry point for the Tauqeet library.
 *
 * Create one instance per user configuration and reuse it for daily calculations.
 * All optional constructor parameters have sensible defaults and are validated on
 * construction — a [TauqeetException] is thrown immediately if any value is out of range.
 *
 * @param method              The Islamic authority convention for Fajr/Isha angles.
 *                            Default: [CalculationMethod.KARACHI]
 * @param madhab              The juristic school for the Asr shadow-length multiplier.
 *                            Default: [Madhab.HANAFI]
 * @param highLatitudeRule    Fallback estimation strategy for sub-polar / polar regions.
 *                            Default: [HighLatitudeRule.MIDDLE_OF_NIGHT]
 * @param elevationMeters     Observer's altitude above sea level in metres (range: −500 to 9000).
 *                            Affects the atmospheric dip and therefore Sunrise/Sunset timing.
 *                            Default: 0.0
 * @param temperatureC        Ambient temperature in Celsius (range: −90 to 80).
 *                            Affects atmospheric refraction near the horizon.
 *                            Default: 12.714
 * @param pressureMbar        Atmospheric pressure in millibars (range: 100 to 1100).
 *                            Affects atmospheric refraction near the horizon.
 *                            Default: 1010.0
 * @param customMethodParams  Override the angles and intervals of [method] with fully custom
 *                            values.  When non-null, [method] is ignored for angle computation
 *                            but its name is replaced by "CUSTOM" in metadata.
 *                            Default: null
 *
 * @throws TauqeetException if any parameter is outside its valid range.
 */
class Tauqeet(
    val method: CalculationMethod = CalculationMethod.KARACHI,
    val madhab: Madhab = Madhab.HANAFI,
    val highLatitudeRule: HighLatitudeRule = HighLatitudeRule.MIDDLE_OF_NIGHT,
    val elevationMeters: Double = 0.0,
    val temperatureC: Double = 12.714,
    val pressureMbar: Double = 1010.0,
    val customMethodParams: com.tauqeet.library.prayers.CalculationMethodParameters? = null
) {
    // Validate constructor-level environmental parameters immediately on construction.
    init {
        validateElevation(elevationMeters)
        validateTemperature(temperatureC)
        validatePressure(pressureMbar)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Prayer Times
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Computes the Islamic prayer times for a given date and geographical location.
     *
     * The internal engine always works in **UTC**. When [timezoneOffset] is omitted
     * (or explicitly set to `0.0`), the returned millisecond timestamps are relative
     * to UTC midnight. Pass the observer's local UTC offset to receive local-time
     * millisecond values instead.
     *
     * @param year              The Gregorian year (must be ≥ 1).
     * @param month             The Gregorian month (1–12).
     * @param day               The day of the month (1–28/29/30/31 depending on the month).
     * @param lat               Observer's latitude in decimal degrees (−90.0 to 90.0).
     *                          **Required.**
     * @param lng               Observer's longitude in decimal degrees (−180.0 to 180.0).
     *                          **Required.**
     * @param timezoneOffset    Hours offset from UTC (−12.0 to +14.0).
     *                          Use decimal fractions for non-whole offsets:
     *                          `5.5` = UTC+5:30 (India), `5.75` = UTC+5:45 (Nepal).
     *                          **Optional. Default: 0.0 (UTC).**
     *                          When omitted, all returned times are in UTC.
     * @param includeAdvancedMetadata  When `true`, populates [PrayerTimesResult.astronomicalMetadata]
     *                          with per-event VSOP87 solver variables (declination, equation of time,
     *                          refraction, etc.). Slightly increases allocation cost.
     *                          **Optional. Default: false.**
     *
     * @return [PrayerTimesResult] where each time field is milliseconds since midnight
     *         in the requested timezone (UTC when [timezoneOffset] = 0.0).
     *
     * @throws TauqeetException if any input parameter is outside its valid range.
     */
    @Deprecated(
        message = "Use the unified DSL prayerRequest { ... } overload instead.",
        replaceWith = ReplaceWith(
            "computePrayerTimes(prayerRequest { latitude = lat; longitude = lng; date = DateComponents(year, month, day); timeZoneOffset = timezoneOffset; includeAdvancedMetadata = includeAdvancedMetadata })"
        )
    )
    fun computePrayerTimes(
        year: Int,
        month: Int,
        day: Int,
        lat: Double,
        lng: Double,
        timezoneOffset: Double = 0.0,
        includeAdvancedMetadata: Boolean = false
    ): PrayerTimesResult = computePrayerTimes(
        prayerRequest {
            this.latitude = lat
            this.longitude = lng
            this.date = DateComponents(year, month, day)
            this.timeZoneOffset = timezoneOffset
            this.includeAdvancedMetadata = includeAdvancedMetadata
        }
    )

    /**
     * Convenience overload that accepts a [DateComponents] object instead of three integers.
     *
     * All parameter contracts are identical to the primary [computePrayerTimes] overload.
     *
     * @throws TauqeetException if any input parameter is outside its valid range.
     */
    @Deprecated(
        message = "Use the unified DSL prayerRequest { ... } overload instead.",
        replaceWith = ReplaceWith(
            "computePrayerTimes(prayerRequest { latitude = lat; longitude = lng; date = date; timeZoneOffset = timezoneOffset; includeAdvancedMetadata = includeAdvancedMetadata })"
        )
    )
    fun computePrayerTimes(
        date: DateComponents,
        lat: Double,
        lng: Double,
        timezoneOffset: Double = 0.0,
        includeAdvancedMetadata: Boolean = false
    ): PrayerTimesResult = computePrayerTimes(
        prayerRequest {
            this.latitude = lat
            this.longitude = lng
            this.date = date
            this.timeZoneOffset = timezoneOffset
            this.includeAdvancedMetadata = includeAdvancedMetadata
        }
    )

    /**
     * Modern overload that encapsulates all inputs into a clean [com.tauqeet.library.prayers.PrayerRequest].
     * Avoids parameter-ordering errors and makes the call site much cleaner.
     *
     * @throws TauqeetException if any input parameter inside the request is outside its valid range.
     */
    fun computePrayerTimes(request: PrayerRequest): PrayerTimesResult {
        val jd = dateToJulianDay(request.date.year, request.date.month, request.date.day.toDouble())
        val paramsToUse = request.calculationParameters.customMethodParams ?: request.calculationParameters.method.params
        val methodName = if (request.calculationParameters.customMethodParams != null) "CUSTOM" else request.calculationParameters.method.name

        val rawResult = internalComputePrayerTimes(
            request.latitude, request.longitude, jd, paramsToUse, methodName,
            request.calculationParameters.madhab, request.calculationParameters.highLatitudeRule,
            request.calculationParameters.elevationMeters, request.calculationParameters.temperatureC, request.calculationParameters.pressureMbar,
            request.includeAdvancedMetadata
        )

        val tzOffsetMs = (request.timeZoneOffset * 3600000.0).toLong()
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
            astronomicalMetadata = rawResult.astronomicalMetadata,
            flags = rawResult.flags,
            resolutionInfo = rawResult.resolutionInfo ?: resolveResolutionInfo(request)
        )
    }

    private fun resolveResolutionInfo(request: PrayerRequest): ResolutionInfo =
        ResolutionInfo(solver = SolverKind.NORMAL, ruleApplied = request.calculationParameters.highLatitudeRule)

    // ─────────────────────────────────────────────────────────────────────────
    // Qibla
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Computes the precise Qibla bearing from a given location to the Kaaba (Mecca)
     * using the WGS-84 Vincenty Inverse formula, with an automatic Spherical Law of
     * Cosines fallback for near-antipodal positions.
     *
     * @param lat Observer's latitude in decimal degrees (−90.0 to 90.0). **Required.**
     * @param lng Observer's longitude in decimal degrees (−180.0 to 180.0). **Required.**
     * @return A [QiblaResult] containing `bearing` (degrees from True North) and
     *         `distanceKm`, or `null` if the observer is at the Kaaba (distance < 1 m).
     *
     * @throws TauqeetException if [lat] or [lng] is out of range.
     */
    fun qiblaDirection(lat: Double, lng: Double): QiblaResult? {
        validateLatitude(lat)
        validateLongitude(lng)
        return tauqeetQibla(lat, lng)
    }

    /**
     * Returns only the Qibla bearing in degrees from True North.
     * Backward-compatible alias for [qiblaDirection].
     *
     * @param lat Observer's latitude in decimal degrees (−90.0 to 90.0). **Required.**
     * @param lng Observer's longitude in decimal degrees (−180.0 to 180.0). **Required.**
     * @return Bearing in degrees, or `null` if the observer is at the Kaaba.
     *
     * @throws TauqeetException if [lat] or [lng] is out of range.
     */
    fun qiblaBearing(lat: Double, lng: Double): Double? {
        validateLatitude(lat)
        validateLongitude(lng)
        return bearingToMecca(lat, lng)
    }
}
