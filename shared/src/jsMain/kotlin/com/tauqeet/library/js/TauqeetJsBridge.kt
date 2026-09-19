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
    val fajr: Long?,
    val sunrise: Long?,
    val dhahwaKubra: Long?,
    val dhuhr: Long?,
    val asr: Long?,
    val maghrib: Long?,
    val isha: Long?,
    val status: String?,
    val method: String?,
    val madhab: String?,
    val highLatitudeRule: String?
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
    val result = Tauqeet(
        method = CalculationMethod.valueOf(method),
        madhab = Madhab.valueOf(madhab),
        highLatitudeRule = HighLatitudeRule.valueOf(highLatitudeRule)
    ).computePrayerTimes(
        prayerRequest {
            latitude = lat
            longitude = lng
            date = DateComponents(year, month, day)
            timeZoneOffset = timezoneOffset
            calculation {
                this.method = CalculationMethod.valueOf(method)
                this.madhab = Madhab.valueOf(madhab)
                this.highLatitudeRule = HighLatitudeRule.valueOf(highLatitudeRule)
            }
        }
    )

    return PrayerTimesJsResult(
        fajr = result.fajr,
        sunrise = result.sunrise,
        dhahwaKubra = result.dhahwaKubra,
        dhuhr = result.dhuhr,
        asr = result.asr,
        maghrib = result.maghrib,
        isha = result.isha,
        status = result.status.name,
        method = result.metadata?.method ?: method,
        madhab = result.metadata?.madhab ?: madhab,
        highLatitudeRule = result.metadata?.highLatitudeRule ?: highLatitudeRule
    )
}
