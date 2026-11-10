@file:JsExport

package com.tauqeet.library.js

import com.tauqeet.library.DateComponents
import com.tauqeet.library.Tauqeet
import com.tauqeet.library.prayers.CalculationMethod
import com.tauqeet.library.prayers.HighLatitudeRule
import com.tauqeet.library.prayers.Madhab
import com.tauqeet.library.prayers.prayerRequest
import kotlin.js.JsExport

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

@JsExport
object TauqeetJsBridge {
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
