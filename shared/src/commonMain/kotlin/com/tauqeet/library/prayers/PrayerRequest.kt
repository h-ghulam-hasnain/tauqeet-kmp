package com.tauqeet.library.prayers

import com.tauqeet.library.DateComponents

/**
 * Encapsulates the configuration parameters for prayer time calculation.
 */
data class PrayerCalculationParameters(
    val method: CalculationMethod = CalculationMethod.KARACHI,
    val madhab: Madhab = Madhab.HANAFI,
    val highLatitudeRule: HighLatitudeRule = HighLatitudeRule.MIDDLE_OF_NIGHT,
    val elevationMeters: Double = 0.0,
    val temperatureC: Double = 12.714,
    val pressureMbar: Double = 1010.0,
    val customMethodParams: CalculationMethodParameters? = null
)

/**
 * Encapsulates all calculation inputs (latitude, longitude, date, timeZoneOffset, calculationParameters)
 * to improve API ergonomics and reduce parameter-ordering errors.
 */
data class PrayerRequest(
    val latitude: Double,
    val longitude: Double,
    val date: DateComponents,
    val timeZoneOffset: Double = 0.0,
    val calculationParameters: PrayerCalculationParameters = PrayerCalculationParameters(),
    val includeAdvancedMetadata: Boolean = false
)
