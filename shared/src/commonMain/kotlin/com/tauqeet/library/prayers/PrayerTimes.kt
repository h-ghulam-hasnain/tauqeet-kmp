package com.tauqeet.library.prayers

import com.tauqeet.library.astronomy.computeSolarPosition
import com.tauqeet.library.toISOTimeString
import kotlin.math.abs

data class PrayerTimesMetadata(
    val method: String,
    val madhab: String,
    val highLatitudeRule: String,
    val isPolarDay: Boolean,
    val isPolarNight: Boolean
)

enum class PrayerStatus {
    SUCCESS,
    CONTINUOUS_TWILIGHT,
    POLAR_DAY,
    POLAR_NIGHT,
    REGIONAL_FALLBACK,
    FAILED
}

data class TwilightMetadata(
    val status: PrayerStatus,
    val DEC_deg: Double?,
    val EOT_min: Double?,
    val angle_deg: Double?,
    val iterations: Int?
)

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

data class DhahwaKubraMetadata(
    val status: PrayerStatus,
    val fajrTimeHr: Double?,
    val maghribTimeHr: Double?
)

data class DhuhrMetadata(
    val status: PrayerStatus,
    val EOT_min: Double,
    val iterations: Int
)

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

data class AstronomicalMetadata(
    val fajr: TwilightMetadata?,
    val sunrise: SunriseSunsetMetadata?,
    val dhahwaKubra: DhahwaKubraMetadata?,
    val dhuhr: DhuhrMetadata?,
    val asr: AsrMetadata?,
    val maghrib: SunriseSunsetMetadata?,
    val isha: TwilightMetadata?
)

data class PrayerTimesResult(
    val fajr: Long?,
    val sunrise: Long?,
    val dhahwaKubra: Long?,
    val dhuhr: Long?,
    val asr: Long?,
    val maghrib: Long?,
    val isha: Long?,
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
}

enum class SolverKind { NORMAL, HIGH_LATITUDE, POLAR_DAY, POLAR_NIGHT }

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
 * Computes the exact prayer times for a given date, location, and method.
 * Uses a standard iterative convergence over the day's changing solar position.
 * Returns the times in UTC hours since midnight (0.0 to 24.0).
 * Multiply by 60 for minutes since midnight.
 */
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
    includeAdvancedMetadata: Boolean = false
): PrayerTimesResult {
    return computePrayerTimes(lat, lng, jd, method.params, method.name, madhab, highLatRule, elevationMeters, temperatureC, pressureMbar, includeAdvancedMetadata)
}

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
    includeAdvancedMetadata: Boolean = false
): PrayerTimesResult {
    val solver = IterativeSolver(jd, lat, lng)
    
    val initialDhuhr = 12.0 - lng / 15.0
    val dhuhrRes = solver.solve(0, initialDhuhr) { 0.0 }
    val dhuhrHr = dhuhrRes?.hours ?: initialDhuhr

    // Get SP at transit for Asr calculations
    val dhuhrJd = jd + dhuhrHr / 24.0
    val dhuhrApproxYear = 2000.0 + (dhuhrJd - 2451545.0) / 365.25
    val dhuhrDeltaT = com.tauqeet.library.time.calculateDeltaT(dhuhrApproxYear)
    val dhuhrJ0 = kotlin.math.floor(dhuhrJd - 0.5) + 0.5
    val dhuhrUt = (dhuhrJd - dhuhrJ0) * 24.0
    val transitSp = computeSolarPosition(dhuhrJ0, dhuhrUt, dhuhrDeltaT)

    val srSsSolver = SunriseSunsetSolver(solver, elevationMeters, temperatureC, pressureMbar)
    val sunriseRes = srSsSolver.solveSunrise(dhuhrHr)
    val sunriseHr = sunriseRes?.hours

    val sunsetRes = srSsSolver.solveSunset(dhuhrHr)
    val sunsetHr = sunsetRes?.hours

    val fajrRes = solver.solve(-1, dhuhrHr - 8.0) { 90.0 + methodParams.fajrAngle }
    val fajrHr = fajrRes?.hours

    val ishaRes = if (methodParams.ishaInterval > 0 && sunsetHr != null) null else solver.solve(1, dhuhrHr + 8.0) { 90.0 + methodParams.ishaAngle }
    val ishaHr = if (methodParams.ishaInterval > 0 && sunsetHr != null) {
        sunsetHr + methodParams.ishaInterval / 60.0
    } else {
        ishaRes?.hours
    }

    val asrSolver = AsrSolver(solver, lat, madhab, temperatureC, pressureMbar, transitSp)
    val asrRes = asrSolver.solve(dhuhrHr)
    val asrHr = asrRes?.hours

    val maghribRes = if (methodParams.maghribInterval > 0 && sunsetHr != null) {
        null
    } else if (methodParams.maghribAngle > 0.0) {
        solver.solve(1, dhuhrHr + 6.5) { 90.0 + methodParams.maghribAngle }
    } else {
        null
    }

    val maghribHr = if (methodParams.maghribInterval > 0 && sunsetHr != null) {
        sunsetHr + methodParams.maghribInterval / 60.0
    } else if (methodParams.maghribAngle > 0.0) {
        maghribRes?.hours
    } else {
        sunsetHr
    }

    val highLatitudeResolver = HighLatitudeResolver(highLatRule, methodParams)
    val hlResult = highLatitudeResolver.resolve(fajrHr, sunriseHr, sunsetHr, ishaHr, dhuhrHr)

    val finalFajr = hlResult.fajr
    val finalSunrise = hlResult.sunrise
    val finalSunset = hlResult.sunset
    val finalIsha = hlResult.isha
    val isPolarDay = hlResult.isPolarDay
    val isPolarNight = hlResult.isPolarNight
    val fallbackApplied = finalFajr == null || finalIsha == null || finalSunrise == null || finalSunset == null
    val finalMaghrib = maghribHr ?: finalSunset
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
            if (isPolarDay && res == null && !isInterval) return PrayerStatus.POLAR_DAY
            if (res == null && !isInterval) return PrayerStatus.CONTINUOUS_TWILIGHT
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

    return PrayerTimesResult(
        fajr = finalFajr?.let { (it * hoursToMs).toLong() },
        sunrise = finalSunrise?.let { (it * hoursToMs).toLong() },
        dhahwaKubra = dhahwaKubraHr?.let { (it * hoursToMs).toLong() },
        dhuhr = dhuhrHr.let { (it * hoursToMs).toLong() },
        asr = finalAsr?.let { (it * hoursToMs).toLong() },
        maghrib = finalMaghrib?.let { (it * hoursToMs).toLong() },
        isha = finalIsha?.let { (it * hoursToMs).toLong() },
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

data class PrayerTimesISO(
    val fajr: String,
    val sunrise: String,
    val dhahwaKubra: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String
)

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
