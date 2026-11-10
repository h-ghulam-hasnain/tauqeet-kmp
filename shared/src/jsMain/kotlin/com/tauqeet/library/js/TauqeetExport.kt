@file:JsExport
@file:Suppress("NON_EXPORTABLE_TYPE")

package com.tauqeet.library

import kotlinx.coroutines.GlobalScope
import kotlin.js.Promise
import kotlinx.datetime.Instant
import kotlinx.coroutines.promise

/** Simple DTO that maps cleanly to a JavaScript object */
@JsExport
data class PrayerTimes(
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String
)

/** Public suspend function exposed as a JavaScript Promise */
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

/** Expose Qibla bearing as a simple number (degrees) */
@JsExport
fun qiblaBearing(lat: Double, lng: Double): Double? = Tauqeet().qiblaBearing(lat, lng)

/** Helper converting Long to String to avoid JS precision loss */
@JsExport
fun longToString(value: Long): String = value.toString()
