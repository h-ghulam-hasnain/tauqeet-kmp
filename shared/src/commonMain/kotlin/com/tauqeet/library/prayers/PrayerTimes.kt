package com.tauqeet.library.prayers

import com.tauqeet.library.TauqeetException
import com.tauqeet.library.astronomy.computeSolarPosition
import com.tauqeet.library.toISOTimeString
import kotlin.math.abs
import kotlin.math.roundToLong
import kotlin.math.floor
import kotlin.math.ceil
import kotlin.math.round
import kotlinx.serialization.Serializable

/** Calculation metadata attached to every [PrayerTimesResult]: the method, madhab, high-latitude rule, and polar-condition flags used for a given computation. */
@Serializable
data class PrayerTimesMetadata(
    val method: String,
    val madhab: String,
    val highLatitudeRule: String,
    val isPolarDay: Boolean,
    val isPolarNight: Boolean
)

@Serializable
enum class PrayerStatus {
    SUCCESS,
    CONTINUOUS_TWILIGHT,
    POLAR_DAY,
    POLAR_NIGHT,
    REGIONAL_FALLBACK,
    FAILED
}

/** Per-event astronomical diagnostic data for a twilight prayer (Fajr or Isha): solver status, solar declination, equation of time, zenith angle, and iteration count. */
@Serializable
data class TwilightMetadata(
    val status: PrayerStatus,
    val DEC_deg: Double?,
    val EOT_min: Double?,
    val angle_deg: Double?,
    val iterations: Int?
)

/** Per-event astronomical diagnostic data for Sunrise or Sunset/Maghrib: solver status, declination, equation of time, horizontal parallax, semi-diameter, atmospheric refraction, observer elevation, and iteration count. */
@Serializable
data class SunriseSunsetMetadata(
    val status: PrayerStatus,
    val DEC_deg: Double?,
    val EOT_min: Double?,
    val HP_arcmin: Double?,
    val SD_arcmin: Double?,
    val refraction_deg: Double?,
    val elevationMeters: Double,
    val iterations: Int?
)

/** Diagnostic data for Dhahwa Kubra (the midpoint between Fajr and Maghrib): solver status and the two source times used to compute the midpoint. */
@Serializable
data class DhahwaKubraMetadata(
    val status: PrayerStatus,
    val fajrTimeHr: Double?,
    val maghribTimeHr: Double?
)

/** Diagnostic data for Dhuhr (solar transit): solver status, equation of time in minutes, and iteration count. */
@Serializable
data class DhuhrMetadata(
    val status: PrayerStatus,
    val EOT_min: Double,
    val iterations: Int
)

/** Diagnostic data for Asr: solver status, solar declination at Dhuhr and Asr, equation of time, semi-diameters, atmospheric refractions, shadow-angle, and iteration count. */
@Serializable
data class AsrMetadata(
    val status: PrayerStatus,
    val DEC_of_Dhuhr_deg: Double?,
    val DEC_of_Asr_deg: Double?,
    val EOT_min: Double?,
    val SD_of_Dhuhr_arcmin: Double?,
    val SD_of_Asr_arcmin: Double?,
    val refraction_of_Dhuhr_deg: Double?,
    val refraction_of_Asr_deg: Double?,
    val asrAngle_deg: Double?,
    val iterations: Int?
)

/** Container holding per-prayer [TwilightMetadata], [SunriseSunsetMetadata], [DhahwaKubraMetadata], [DhuhrMetadata], and [AsrMetadata] diagnostic records for a single computation. Only populated when `includeAdvancedMetadata = true`. */
@Serializable
data class AstronomicalMetadata(
    val fajr: TwilightMetadata?,
    val sunrise: SunriseSunsetMetadata?,
    val dhahwaKubra: DhahwaKubraMetadata?,
    val dhuhr: DhuhrMetadata?,
    val asr: AsrMetadata?,
    val maghrib: SunriseSunsetMetadata?,
    val isha: TwilightMetadata?
)

/**
 * Result of a single prayer-time computation for one day and location.
 *
 * All time fields are milliseconds since **UTC midnight** of the requested date,
 * adjusted by the caller's `timeZoneOffset`. A `null` value means the event
 * could not be computed (e.g., polar night, continuous twilight).
 *
 * @property fajr            Fajr (pre-dawn twilight) time in milliseconds, or `null`.
 * @property sunrise         Sunrise time in milliseconds, or `null`.
 * @property dhahwaKubra     Dhahwa Kubra (midpoint of Fajr–Sunset) in milliseconds, or `null`.
 * @property dhuhr           Dhuhr (solar transit) time in milliseconds, or `null` during polar night.
 * @property asr             Asr time in milliseconds, or `null`.
 * @property maghrib         Maghrib (sunset) time in milliseconds, or `null`.
 * @property isha            Isha (evening twilight) time in milliseconds, or `null`.
 * @property status          Overall computation status. See [PrayerStatus] for possible values.
 * @property metadata        Calculation metadata (method, madhab, high-latitude rule, polar flags).
 *                           Always populated.
 * @property astronomicalMetadata  Per-prayer VSOP87 solver diagnostics. `null` unless
 *                           `includeAdvancedMetadata = true` was passed to [computePrayerTimes].
 * @property flags           Bitmask of condition flags. Use [isPolarDay], [isPolarNight],
 *                           and [isHighLatitudeFallback] for type-safe access.
 * @property resolutionInfo  Which solver branch was used and which high-latitude rule was applied.
 * @property isPolarDay      `true` when the sun does not set (continuous daylight). Fajr/Isha are `null`.
 * @property isPolarNight    `true` when the sun does not rise (continuous darkness). Most prayers are `null`.
 * @property isHighLatitudeFallback  `true` when at least one prayer used a high-latitude estimation rule.
 */
@Serializable
data class PrayerTimesResult(
    val fajr: Long?,
    val sunrise: Long?,
    val dhahwaKubra: Long?,
    val dhuhr: Long?,
    val asr: Long?,
    val maghrib: Long?,
    val isha: Long?,
    val status: PrayerStatus = PrayerStatus.SUCCESS,
    val metadata: PrayerTimesMetadata? = null,
    val astronomicalMetadata: AstronomicalMetadata? = null,
    val flags: Int = 0,
    val resolutionInfo: ResolutionInfo? = null
) {
    companion object {
        const val FLAG_POLAR_DAY = 1 shl 0
        const val FLAG_POLAR_NIGHT = 1 shl 1
        const val FLAG_HIGH_LATITUDE_FALLBACK = 1 shl 2
    }

    val isPolarDay: Boolean
        get() = (flags and FLAG_POLAR_DAY) != 0

    val isPolarNight: Boolean
        get() = (flags and FLAG_POLAR_NIGHT) != 0

    val isHighLatitudeFallback: Boolean
        get() = (flags and FLAG_HIGH_LATITUDE_FALLBACK) != 0

    /**
     * Returns a human-readable multi-line summary of this result, suitable for logging or debugging.
     *
     * Includes overall status, calculation method/madhab/high-latitude rule (when [metadata] is
     * present), key astronomical values (when [astronomicalMetadata] is present), and the solver
     * kind from [resolutionInfo].
     *
     * Example output:
     * ```
     * === Prayer Times Summary ===
     * Status: SUCCESS
     * Method: KARACHI
     * Madhab: HANAFI
     * High Latitude Rule: MIDDLE_OF_NIGHT
     * Solver: NORMAL
     * ```
     *
     * @return A formatted multi-line string. The exact format is not stable across versions.
     */
    fun toSummaryString(): String {
        val meta = metadata
        val astro = astronomicalMetadata
        val sb = StringBuilder()
        sb.appendLine("=== Prayer Times Summary ===")
        sb.appendLine("Status: \$status")
        if (meta != null) {
            sb.appendLine("Method: \${meta.method}")
            sb.appendLine("Madhab: \${meta.madhab}")
            sb.appendLine("High Latitude Rule: \${meta.highLatitudeRule}")
        }
        if (astro != null) {
            sb.appendLine("--- Astronomical Data ---")
            sb.appendLine("Sunrise DEC: \${astro.sunrise?.DEC_deg?.let { \"\${it}°\" } ?: \"N/A\"}")
            sb.appendLine("Dhuhr EOT: \${astro.dhuhr?.EOT_min?.let { \"\${it} min\" } ?: \"N/A\"}")
            sb.appendLine("Sunrise Refraction: \${astro.sunrise?.refraction_deg?.let { \"\${it}°\" } ?: \"N/A\"}")
        }
        if (resolutionInfo != null) {
            sb.appendLine("Solver: \${resolutionInfo.solver}")
        }
        return sb.toString()
    }
}

@Serializable
enum class SolverKind { NORMAL, HIGH_LATITUDE, POLAR_DAY, POLAR_NIGHT }

/** Records which internal solver branch was chosen for a computation and which high-latitude rule (if any) was applied. Attached to every [PrayerTimesResult.resolutionInfo]. */
@Serializable
data class ResolutionInfo(
    val solver: SolverKind,
    val ruleApplied: HighLatitudeRule? = null
)

private fun resolveSolver(
    lat: Double,
    solarDeclination: Double,
    sunriseHr: Double?,
    sunsetHr: Double?,
    fajrHr: Double?,
    ishaHr: Double?,
    highLatRule: HighLatitudeRule
): ResolutionInfo {
    val absLat = abs(lat)
    val absDeclination = abs(solarDeclination)
    val polarBoundary = absLat + absDeclination >= 90.0
    val polarDay = polarBoundary && ((lat > 0.0 && solarDeclination > 0.0) || (lat < 0.0 && solarDeclination < 0.0)) && (fajrHr == null || ishaHr == null)
    val polarNight = polarBoundary && ((lat > 0.0 && solarDeclination < 0.0) || (lat < 0.0 && solarDeclination > 0.0)) && (sunriseHr == null || sunsetHr == null)
    val fallbackApplied = sunriseHr == null || sunsetHr == null || fajrHr == null || ishaHr == null

    val solver = when {
        polarDay -> SolverKind.POLAR_DAY
        polarNight -> SolverKind.POLAR_NIGHT
        fallbackApplied -> SolverKind.HIGH_LATITUDE
        else -> SolverKind.NORMAL
    }

    return ResolutionInfo(solver = solver, ruleApplied = highLatRule)
}

/**
 * Computes Islamic prayer times for the given date, location, and [CalculationMethod].
 *
 * Convenience overload that resolves [method] to its [CalculationMethodParameters] automatically.
 * Delegates to the primary [computePrayerTimes] overload that accepts raw parameters.
 *
 * @param lat                  Observer's latitude in decimal degrees (−90.0 to 90.0).
 * @param lng                  Observer's longitude in decimal degrees (−180.0 to 180.0).
 * @param jd                   Julian Day number for the requested date (use [dateToJulianDay] to obtain).
 * @param method               The [CalculationMethod] whose angle/interval parameters to use.
 * @param madhab               Juristic school for the Asr shadow multiplier. Default: [Madhab.SHAFI].
 * @param highLatRule          High-latitude fallback strategy. Default: [HighLatitudeRule.MIDDLE_OF_NIGHT].
 * @param elevationMeters      Observer's altitude above sea level in metres. Default: 0.0.
 * @param temperatureC         Ambient temperature in Celsius for refraction correction. Default: 10.0.
 * @param pressureMbar         Atmospheric pressure in millibars for refraction correction. Default: 1010.0.
 * @param includeAdvancedMetadata  When `true`, populates [PrayerTimesResult.astronomicalMetadata]. Default: false.
 * @param roundingStrategy     Rounding convention applied to raw millisecond values. Default: [RoundingStrategy.FIQH_IHTIYAT].
 * @return [PrayerTimesResult] with millisecond-UTC timestamps for each prayer.
 * @throws TauqeetException if any parameter is outside its valid range.
 */
@Throws(TauqeetException::class)
fun computePrayerTimes(
    lat: Double,
    lng: Double,
    jd: Double,
    method: CalculationMethod,
    madhab: Madhab = Madhab.SHAFI,
    highLatRule: HighLatitudeRule = HighLatitudeRule.MIDDLE_OF_NIGHT,
    elevationMeters: Double = 0.0,
    temperatureC: Double = 10.0,
    pressureMbar: Double = 1010.0,
    includeAdvancedMetadata: Boolean = false,
    roundingStrategy: RoundingStrategy = RoundingStrategy.FIQH_IHTIYAT
): PrayerTimesResult {
    return computePrayerTimes(lat, lng, jd, method.params, method.name, madhab, highLatRule, elevationMeters, temperatureC, pressureMbar, includeAdvancedMetadata, roundingStrategy)
}

/**
 * Primary prayer-time computation engine.
 *
 * Runs a full VSOP87/IAU-2000B ephemeris pipeline: it derives the solar transit (Dhuhr)
 * via iterative convergence, computes Sunrise/Sunset with atmospheric refraction and
 * observer-elevation corrections, then solves Fajr, Isha, Asr, and Maghrib using the
 * supplied method angles/intervals. High-latitude fallback rules are applied last.
 *
 * @param lat                  Observer's latitude in decimal degrees (−90.0 to 90.0).
 * @param lng                  Observer's longitude in decimal degrees (−180.0 to 180.0).
 * @param jd                   Julian Day number for the requested date.
 * @param methodParams         Raw angle and interval parameters for the calculation authority.
 * @param methodName           Display name stored in [PrayerTimesResult.metadata]. Default: `"CUSTOM"`.
 * @param madhab               Juristic school for the Asr shadow multiplier. Default: [Madhab.SHAFI].
 * @param highLatRule          High-latitude fallback strategy. Default: [HighLatitudeRule.MIDDLE_OF_NIGHT].
 * @param elevationMeters      Observer's altitude above sea level in metres. Default: 0.0.
 * @param temperatureC         Ambient temperature in Celsius for refraction correction. Default: 10.0.
 * @param pressureMbar         Atmospheric pressure in millibars for refraction correction. Default: 1010.0.
 * @param includeAdvancedMetadata  When `true`, populates [PrayerTimesResult.astronomicalMetadata]
 *                             with per-event VSOP87 solver diagnostics. Default: false.
 * @param roundingStrategy     Rounding convention applied to raw millisecond values. Default: [RoundingStrategy.FIQH_IHTIYAT].
 * @return [PrayerTimesResult] with millisecond-UTC timestamps for each prayer, overall [PrayerStatus],
 *         calculation [PrayerTimesMetadata], and optional [AstronomicalMetadata].
 * @throws TauqeetException if any parameter is outside its valid range.
 */
@Throws(TauqeetException::class)
fun computePrayerTimes(
    lat: Double,
    lng: Double,
    jd: Double,
    methodParams: CalculationMethodParameters,
    methodName: String = "CUSTOM",
    madhab: Madhab = Madhab.SHAFI,
    highLatRule: HighLatitudeRule = HighLatitudeRule.MIDDLE_OF_NIGHT,
    elevationMeters: Double = 0.0,
    temperatureC: Double = 10.0,
    pressureMbar: Double = 1010.0,
    includeAdvancedMetadata: Boolean = false,
    roundingStrategy: RoundingStrategy = RoundingStrategy.FIQH_IHTIYAT
): PrayerTimesResult {
    val solver = IterativeSolver(jd, lat, lng)
    
    val initialDhuhr = 12.0 - lng / 15.0
    val dhuhrRes = solver.solve(0, initialDhuhr) { 0.0 }
    val dhuhrHr = if (dhuhrRes?.error == true) initialDhuhr else (dhuhrRes?.hours ?: initialDhuhr)

    // Get SP at transit for Asr calculations
    val dhuhrJd = jd + dhuhrHr / 24.0
    val dhuhrApproxYear = 2000.0 + (dhuhrJd - 2451545.0) / 365.25
    val dhuhrDeltaT = com.tauqeet.library.time.calculateDeltaT(dhuhrApproxYear)
    val dhuhrJ0 = kotlin.math.floor(dhuhrJd - 0.5) + 0.5
    val dhuhrUt = (dhuhrJd - dhuhrJ0) * 24.0
    val transitSp = computeSolarPosition(dhuhrJ0, dhuhrUt, dhuhrDeltaT)

    val srSsSolver = SunriseSunsetSolver(solver, elevationMeters, temperatureC, pressureMbar)
    val sunriseRes = srSsSolver.solveSunrise(dhuhrHr)
    val sunriseHr = if (sunriseRes?.error == true) null else sunriseRes?.hours

    val sunsetRes = srSsSolver.solveSunset(dhuhrHr)
    val sunsetHr = if (sunsetRes?.error == true) null else sunsetRes?.hours

    val fajrRes = solver.solve(-1, 6.0 - lng / 15.0) { 90.0 + methodParams.fajrAngle }
    val fajrHr = if (fajrRes?.error == true) null else fajrRes?.hours

    val ishaRes = if (methodParams.ishaInterval > 0 && sunsetHr != null) null else solver.solve(1, 18.0 - lng / 15.0) { 90.0 + methodParams.ishaAngle }
    val ishaHr = if (methodParams.ishaInterval > 0 && sunsetHr != null) {
        sunsetHr + methodParams.ishaInterval / 60.0
    } else {
        if (ishaRes?.error == true) null else ishaRes?.hours
    }

    val asrSolver = AsrSolver(solver, lat, madhab, temperatureC, pressureMbar, transitSp)
    val asrRes = asrSolver.solve(dhuhrHr)
    val asrHr = if (asrRes?.error == true) null else asrRes?.hours

    val maghribRes = if (methodParams.maghribInterval > 0 && sunsetHr != null) {
        null
    } else if (methodParams.maghribAngle > 0.0) {
        solver.solve(1, 18.0 - lng / 15.0) { 90.0 + methodParams.maghribAngle }
    } else {
        null
    }

    val maghribHr = if (methodParams.maghribInterval > 0 && sunsetHr != null) {
        sunsetHr + methodParams.maghribInterval / 60.0
    } else if (methodParams.maghribAngle > 0.0) {
        if (maghribRes?.error == true) null else maghribRes?.hours
    } else {
        sunsetHr
    }

    val highLatitudeResolver = HighLatitudeResolver(highLatRule, methodParams)
    val yesterdaySunsetHr = if (highLatRule == HighLatitudeRule.MIDDLE_OF_NIGHT && sunriseHr != null && sunsetHr != null) {
        val yesterdaySolver = IterativeSolver(jd - 1.0, lat, lng)
        val yesterdaySunsetRes = SunriseSunsetSolver(yesterdaySolver, elevationMeters, temperatureC, pressureMbar).solveSunset(dhuhrHr)
        if (yesterdaySunsetRes?.error == true) null else yesterdaySunsetRes?.hours
    } else {
        null
    }
    val hlResult = highLatitudeResolver.resolve(
        fajrHr, sunriseHr, sunsetHr, ishaHr, dhuhrHr, lat, transitSp.declination, yesterdaySunsetHr
    )

    val finalFajr = hlResult.fajr
    val finalSunrise = hlResult.sunrise
    val finalSunset = hlResult.sunset
    val finalIsha = hlResult.isha
    val isPolarDay = hlResult.isPolarDay
    val isPolarNight = hlResult.isPolarNight
    val fallbackApplied = fajrHr == null || ishaHr == null || sunriseHr == null || sunsetHr == null || maghribHr == null
    
    val finalMaghrib = if (maghribHr != null) {
        maghribHr
    } else if (methodParams.maghribInterval > 0) {
        finalSunset?.plus(methodParams.maghribInterval / 60.0)
    } else {
        finalSunset
    }
    val finalAsr = asrHr
    val solarDeclination = transitSp.declination
    val resolutionInfo = resolveSolver(
        lat = lat,
        solarDeclination = solarDeclination,
        sunriseHr = sunriseHr,
        sunsetHr = sunsetHr,
        fajrHr = fajrHr,
        ishaHr = ishaHr,
        highLatRule = highLatRule
    )

    var flags = 0
    if (isPolarDay || resolutionInfo.solver == SolverKind.POLAR_DAY) {
        flags = flags or PrayerTimesResult.FLAG_POLAR_DAY
    }
    if (isPolarNight || resolutionInfo.solver == SolverKind.POLAR_NIGHT) {
        flags = flags or PrayerTimesResult.FLAG_POLAR_NIGHT
    }
    if (fallbackApplied || resolutionInfo.solver == SolverKind.HIGH_LATITUDE || resolutionInfo.solver == SolverKind.POLAR_DAY || resolutionInfo.solver == SolverKind.POLAR_NIGHT) {
        flags = flags or PrayerTimesResult.FLAG_HIGH_LATITUDE_FALLBACK
    }

    val dhahwaKubraHr: Double? = if (finalFajr != null && finalSunset != null) (finalFajr + finalSunset) / 2.0 else null

    // Times are calculated in UTC decimal hours. We convert to milliseconds since midnight UTC.
    val hoursToMs = 3600000.0
    val astroMeta = if (includeAdvancedMetadata) {
        fun getStatus(res: SolverResult?, isInterval: Boolean = false): PrayerStatus {
            if (isPolarNight) return PrayerStatus.POLAR_NIGHT
            if (isPolarDay && (res == null || res.error) && !isInterval) return PrayerStatus.POLAR_DAY
            if ((res == null || res.error) && !isInterval) return PrayerStatus.CONTINUOUS_TWILIGHT
            return PrayerStatus.SUCCESS
        }

        val dhuhrStatus = getStatus(dhuhrRes)
        val dhuhrMeta = DhuhrMetadata(
            status = dhuhrStatus,
            EOT_min = dhuhrRes?.sp?.equationOfTime ?: 0.0,
            iterations = dhuhrRes?.iterations ?: 0
        )

        val fajrStatus = getStatus(fajrRes)
        val fajrMeta = TwilightMetadata(
            status = fajrStatus,
            DEC_deg = fajrRes?.sp?.declination,
            EOT_min = fajrRes?.sp?.equationOfTime,
            angle_deg = fajrRes?.targetZenith?.let { it - 90.0 },
            iterations = fajrRes?.iterations
        )

        val sunriseStatus = getStatus(sunriseRes)
        val sunriseMeta = SunriseSunsetMetadata(
            status = sunriseStatus,
            DEC_deg = sunriseRes?.sp?.declination,
            EOT_min = sunriseRes?.sp?.equationOfTime,
            HP_arcmin = sunriseRes?.sp?.horizontalParallax,
            SD_arcmin = sunriseRes?.sp?.let { it.semidiameter / 60.0 },
            refraction_deg = sunriseRes?.let { getRefractionDegrees(90.0 - it.targetZenith, temperatureC, pressureMbar) },
            elevationMeters = elevationMeters,
            iterations = sunriseRes?.iterations
        )

        val ishaInterval = methodParams.ishaInterval > 0 && sunsetHr != null
        val ishaStatus = getStatus(ishaRes, ishaInterval)
        val ishaMeta = TwilightMetadata(
            status = ishaStatus,
            DEC_deg = ishaRes?.sp?.declination,
            EOT_min = ishaRes?.sp?.equationOfTime,
            angle_deg = ishaRes?.targetZenith?.let { it - 90.0 },
            iterations = ishaRes?.iterations
        )

        val sunsetStatus = getStatus(sunsetRes)
        val maghribInterval = methodParams.maghribInterval > 0 && sunsetHr != null
        val maghribStatus = if (maghribRes != null) getStatus(maghribRes) else sunsetStatus
        val maghribSource = maghribRes ?: sunsetRes
        val maghribMeta = SunriseSunsetMetadata(
            status = maghribStatus,
            DEC_deg = maghribSource?.sp?.declination,
            EOT_min = maghribSource?.sp?.equationOfTime,
            HP_arcmin = maghribSource?.sp?.horizontalParallax,
            SD_arcmin = maghribSource?.sp?.let { it.semidiameter / 60.0 },
            refraction_deg = maghribSource?.let { getRefractionDegrees(90.0 - it.targetZenith, temperatureC, pressureMbar) },
            elevationMeters = elevationMeters,
            iterations = maghribSource?.iterations
        )

        val asrStatus = getStatus(asrRes)
        val asrMeta = AsrMetadata(
            status = asrStatus,
            DEC_of_Dhuhr_deg = transitSp.declination,
            DEC_of_Asr_deg = asrRes?.sp?.declination,
            EOT_min = asrRes?.sp?.equationOfTime,
            SD_of_Dhuhr_arcmin = transitSp.semidiameter / 60.0,
            SD_of_Asr_arcmin = asrRes?.sp?.let { it.semidiameter / 60.0 },
            refraction_of_Dhuhr_deg = getRefractionDegrees(90.0 - abs(lat - transitSp.declination), temperatureC, pressureMbar),
            refraction_of_Asr_deg = asrRes?.let { getRefractionDegrees(90.0 - it.targetZenith, temperatureC, pressureMbar) },
            asrAngle_deg = asrRes?.targetZenith?.let { it - 90.0 },
            iterations = asrRes?.iterations
        )

        val dhahwaKubraMeta = DhahwaKubraMetadata(
            status = if (dhahwaKubraHr != null) PrayerStatus.SUCCESS else PrayerStatus.FAILED,
            fajrTimeHr = finalFajr,
            maghribTimeHr = finalMaghrib
        )

        AstronomicalMetadata(
            fajr = fajrMeta,
            sunrise = sunriseMeta,
            dhahwaKubra = dhahwaKubraMeta,
            dhuhr = dhuhrMeta,
            asr = asrMeta,
            maghrib = maghribMeta,
            isha = ishaMeta
        )
    } else null

    val overallStatus = when {
        isPolarDay || resolutionInfo.solver == SolverKind.POLAR_DAY -> PrayerStatus.POLAR_DAY
        isPolarNight || resolutionInfo.solver == SolverKind.POLAR_NIGHT -> PrayerStatus.POLAR_NIGHT
        fallbackApplied || resolutionInfo.solver == SolverKind.HIGH_LATITUDE -> PrayerStatus.CONTINUOUS_TWILIGHT
        else -> PrayerStatus.SUCCESS
    }

    val applyRounding: (Double?, Boolean) -> Long? = { timeHr, isFloor ->
        timeHr?.let {
            val ms = it * hoursToMs
            val seconds = ms / 1000.0
            val roundedSeconds = if (roundingStrategy == RoundingStrategy.FIQH_IHTIYAT) {
                if (isFloor) floor(seconds) else ceil(seconds)
            } else {
                round(seconds)
            }
            (roundedSeconds * 1000).toLong()
        }
    }

    return PrayerTimesResult(
        fajr = applyRounding(finalFajr, true),
        sunrise = applyRounding(finalSunrise, true),
        dhahwaKubra = applyRounding(dhahwaKubraHr, true),
        dhuhr = if (isPolarNight) null else applyRounding(dhuhrHr, false),
        asr = applyRounding(finalAsr, false),
        maghrib = applyRounding(finalMaghrib, false),
        isha = applyRounding(finalIsha, false),
        status = overallStatus,
        metadata = PrayerTimesMetadata(
            method = methodName,
            madhab = madhab.name,
            highLatitudeRule = highLatRule.name,
            isPolarDay = isPolarDay,
            isPolarNight = isPolarNight
        ),
        astronomicalMetadata = astroMeta,
        flags = flags,
        resolutionInfo = resolutionInfo
    )
}

/** All seven prayer times from a [PrayerTimesResult] formatted as ISO 8601 time strings (`HH:mm:ss`). Obtain via [PrayerTimesResult.toISOTimes]. */
data class PrayerTimesISO(
    val fajr: String,
    val sunrise: String,
    val dhahwaKubra: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String
)

/**
 * Converts all seven prayer-time millisecond values to ISO 8601 `HH:mm:ss` strings.
 *
 * Times that are `null` (e.g., during polar night or continuous twilight) are represented
 * as `"Invalid Date"` to maintain a stable shape for JSON serialization.
 *
 * Example:
 * ```kotlin
 * val iso = result.toISOTimes()
 * println(iso.fajr)    // "04:32:00"
 * println(iso.sunrise) // "06:15:00"
 * ```
 *
 * @return [PrayerTimesISO] with all seven prayer times as `HH:mm:ss` strings,
 *         or `"Invalid Date"` for any `null` field.
 */
fun PrayerTimesResult.toISOTimes(): PrayerTimesISO {
    return PrayerTimesISO(
        fajr = this.fajr?.toISOTimeString() ?: "Invalid Date",
        sunrise = this.sunrise?.toISOTimeString() ?: "Invalid Date",
        dhahwaKubra = this.dhahwaKubra?.toISOTimeString() ?: "Invalid Date",
        dhuhr = this.dhuhr?.toISOTimeString() ?: "Invalid Date",
        asr = this.asr?.toISOTimeString() ?: "Invalid Date",
        maghrib = this.maghrib?.toISOTimeString() ?: "Invalid Date",
        isha = this.isha?.toISOTimeString() ?: "Invalid Date"
    )
}
