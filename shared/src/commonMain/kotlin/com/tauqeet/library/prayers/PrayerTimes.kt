package com.tauqeet.library.prayers

import com.tauqeet.library.astronomy.computeSolarPosition
import com.tauqeet.library.internal.acosd
import com.tauqeet.library.internal.atand
import com.tauqeet.library.internal.cosd
import com.tauqeet.library.internal.sind
import com.tauqeet.library.internal.tand
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
    val astronomicalMetadata: AstronomicalMetadata? = null
)

/**
 * Calculates the hour angle (in degrees) for a target zenith.
 */
private fun solveHourAngle(targetZenith: Double, lat: Double, dec: Double): Double? {
    val cosH = (cosd(targetZenith) - sind(lat) * sind(dec)) / (cosd(lat) * cosd(dec))
    if (cosH < -1.0 || cosH > 1.0) return null
    return acosd(cosH)
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
    class SolverResult(val hours: Double, val sp: com.tauqeet.library.astronomy.SolarPositionResult, val iterations: Int, val targetZenith: Double)

    fun solveIteratively(side: Int, targetZenith: (com.tauqeet.library.astronomy.SolarPositionResult) -> Double, initialHour: Double): SolverResult? {
        var currentHours = initialHour
        var prevHours = currentHours
        var lastSp: com.tauqeet.library.astronomy.SolarPositionResult? = null
        var lastZenith = 0.0
        var iter = 0

        for (i in 0 until 15) {
            iter++
            val probeJd = jd + currentHours / 24.0
            val approxYear = 2000.0 + (probeJd - 2451545.0) / 365.25
            val deltaT = com.tauqeet.library.time.calculateDeltaT(approxYear)
            
            val j0 = kotlin.math.floor(probeJd - 0.5) + 0.5
            val ut = (probeJd - j0) * 24.0
            val sp = computeSolarPosition(j0, ut, deltaT)
            lastSp = sp
            
            val tz = targetZenith(sp)
            lastZenith = tz
            val transit = 12.0 - lng / 15.0 - sp.equationOfTime / 60.0

            if (side == 0) {
                currentHours = transit
            } else {
                val hDeg = solveHourAngle(tz, lat, sp.declination) ?: return null
                val hHours = hDeg / 15.0
                currentHours = if (side < 0) transit - hHours else transit + hHours
            }

            if (abs(currentHours - prevHours) * 3600 < 0.1) break
            prevHours = currentHours
        }
        return lastSp?.let { SolverResult(currentHours, it, iter, lastZenith) }
    }

    val initialDhuhr = 12.0 - lng / 15.0
    val dhuhrRes = solveIteratively(0, { 0.0 }, initialDhuhr)
    val dhuhrHr = dhuhrRes?.hours ?: initialDhuhr

    // Get SP at transit for Asr calculations
    val dhuhrJd = jd + dhuhrHr / 24.0
    val dhuhrApproxYear = 2000.0 + (dhuhrJd - 2451545.0) / 365.25
    val dhuhrDeltaT = com.tauqeet.library.time.calculateDeltaT(dhuhrApproxYear)
    val dhuhrJ0 = kotlin.math.floor(dhuhrJd - 0.5) + 0.5
    val dhuhrUt = (dhuhrJd - dhuhrJ0) * 24.0
    val transitSp = computeSolarPosition(dhuhrJ0, dhuhrUt, dhuhrDeltaT)

    val dip = computeDipAngle(elevationMeters)
    val refraction0 = getRefractionDegrees(0.0, temperatureC, pressureMbar)
    
    val sunriseSunsetZenithFn: (com.tauqeet.library.astronomy.SolarPositionResult) -> Double = { sp ->
        90.0 + refraction0 + sp.semidiameter / 60.0 - sp.horizontalParallax / 60.0 + dip
    }

    val sunriseRes = solveIteratively(-1, sunriseSunsetZenithFn, dhuhrHr - 6.0)
    val sunriseHr = sunriseRes?.hours

    val sunsetRes = solveIteratively(1, sunriseSunsetZenithFn, dhuhrHr + 6.0)
    val sunsetHr = sunsetRes?.hours

    val fajrRes = solveIteratively(-1, { 90.0 + methodParams.fajrAngle }, dhuhrHr - 8.0)
    val fajrHr = fajrRes?.hours

    val ishaRes = if (methodParams.ishaInterval > 0 && sunsetHr != null) null else solveIteratively(1, { 90.0 + methodParams.ishaAngle }, dhuhrHr + 8.0)
    val ishaHr = if (methodParams.ishaInterval > 0 && sunsetHr != null) {
        sunsetHr + methodParams.ishaInterval / 60.0
    } else {
        ishaRes?.hours
    }

    val asrRes = solveIteratively(1, { sp ->
        val zZuhr = abs(lat - transitSp.declination)
        val sdZuhr = transitSp.semidiameter / 60.0
        val refrZuhr = getRefractionDegrees(90.0 - zZuhr, temperatureC, pressureMbar)
        val zZuhrVisual = zZuhr - refrZuhr - sdZuhr
        
        val zAsrVisual = atand(tand(zZuhrVisual) + madhab.shadowFactor)
        val refrAsr = getRefractionDegrees(90.0 - zAsrVisual, temperatureC, pressureMbar)
        val sdAsr = sp.semidiameter / 60.0
        
        zAsrVisual + refrAsr + sdAsr
    }, dhuhrHr + 4.0)
    val asrHr = asrRes?.hours

    val maghribRes = if (methodParams.maghribInterval > 0 && sunsetHr != null) {
        null
    } else if (methodParams.maghribAngle > 0.0) {
        solveIteratively(1, { 90.0 + methodParams.maghribAngle }, dhuhrHr + 6.5)
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



    // High latitude fallback logic
    var finalFajr: Double? = fajrHr ?: (dhuhrHr - 8.0)
    var finalSunrise: Double? = sunriseHr ?: (dhuhrHr - 6.0)
    var finalSunset: Double? = sunsetHr ?: (dhuhrHr + 6.0)
    var finalMaghrib: Double? = maghribHr ?: finalSunset
    var finalIsha: Double? = ishaHr ?: (dhuhrHr + 8.0)
    var finalAsr: Double? = asrHr
    
    var isPolarDay = false
    var isPolarNight = false

    if (finalSunrise != null && finalSunset != null && sunriseHr != null && sunsetHr != null) {
        var nightDuration = if (finalSunrise!! < finalSunset!!) {
            24.0 - (finalSunset!! - finalSunrise!!)
        } else {
            (finalSunrise!! - finalSunset!!) // shouldn't usually happen with valid data
        }
        
        // Prevent division by zero or NaN issues at polar boundaries
        if (nightDuration < 0.001) {
            nightDuration = 0.001
        }

        if (fajrHr == null || ishaHr == null) {
            isPolarDay = true
            when (highLatRule) {
                HighLatitudeRule.MIDDLE_OF_NIGHT -> {
                    val halfNight = nightDuration / 2.0
                    if (fajrHr == null) finalFajr = finalSunrise!! - halfNight
                    if (ishaHr == null) finalIsha = finalSunset!! + halfNight
                }
                HighLatitudeRule.SEVENTH_OF_NIGHT -> {
                    val seventhNight = nightDuration / 7.0
                    if (fajrHr == null) finalFajr = finalSunrise!! - seventhNight
                    if (ishaHr == null) finalIsha = finalSunset!! + seventhNight
                }
                HighLatitudeRule.TWILIGHT_ANGLE -> {
                    val fajrProportion = methodParams.fajrAngle / 60.0
                    val ishaProportion = methodParams.ishaAngle / 60.0
                    if (fajrHr == null) finalFajr = finalSunrise!! - nightDuration * fajrProportion
                    if (ishaHr == null) finalIsha = finalSunset!! + nightDuration * ishaProportion
                }
            }
        }
    } else {
        isPolarNight = true
    }

    val dhahwaKubraHr: Double? = if (finalFajr != null && finalSunset != null) (finalFajr!! + finalSunset!!) / 2.0 else null

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
        astronomicalMetadata = astroMeta
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
