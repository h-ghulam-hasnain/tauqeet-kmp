package com.tauqeet.library.prayers

import com.tauqeet.library.DateComponents
import com.tauqeet.library.validateDate
import com.tauqeet.library.validateLatitude
import com.tauqeet.library.validateLongitude
import com.tauqeet.library.validateTimezoneOffset

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
 * Public immutable model for a prayer-time request.
 * The primary constructor is internal — callers must use the DSL `prayerRequest { ... }`
 * which ensures validation happens eagerly and keeps the API ergonomic.
 */
data class PrayerRequest internal constructor(
    val latitude: Double,
    val longitude: Double,
    val date: DateComponents,
    val timeZoneOffset: Double,
    val calculationParameters: PrayerCalculationParameters,
    val includeAdvancedMetadata: Boolean
)

/**
 * Inline DSL entry point. The builder is compiled away and remains allocation-light
 * for Kotlin Multiplatform use-cases.
 */
inline fun prayerRequest(builder: PrayerRequestBuilder.() -> Unit): PrayerRequest =
    PrayerRequestBuilder().apply(builder).build()

/**
 * Builder used by the DSL. It performs eager validation using the same internal helpers
 * as the public API, so there is no duplicated validation logic.
 */
class PrayerRequestBuilder {
    var latitude: Double = Double.NaN
    var longitude: Double = Double.NaN
    var date: DateComponents = DateComponents(1970, 1, 1)
    var timeZoneOffset: Double = 0.0
    var calculation: PrayerCalculationParametersBuilder = PrayerCalculationParametersBuilder()
    var includeAdvancedMetadata: Boolean = false

    fun calculation(builder: PrayerCalculationParametersBuilder.() -> Unit) {
        calculation.apply(builder)
    }

    fun build(): PrayerRequest {
        validateLatitude(latitude)
        validateLongitude(longitude)
        validateTimezoneOffset(timeZoneOffset)
        validateDate(date.year, date.month, date.day)

        return PrayerRequest(
            latitude = latitude,
            longitude = longitude,
            date = date,
            timeZoneOffset = timeZoneOffset,
            calculationParameters = calculation.toParams(),
            includeAdvancedMetadata = includeAdvancedMetadata
        )
    }
}

/**
 * Nested builder for calculation-parameter configuration.
 */
class PrayerCalculationParametersBuilder {
    var method: CalculationMethod = CalculationMethod.KARACHI
    var madhab: Madhab = Madhab.HANAFI
    var highLatitudeRule: HighLatitudeRule = HighLatitudeRule.MIDDLE_OF_NIGHT
    var elevationMeters: Double = 0.0
    var temperatureC: Double = 12.714
    var pressureMbar: Double = 1010.0
    var customMethodParams: CalculationMethodParameters? = null

    fun toParams(): PrayerCalculationParameters = PrayerCalculationParameters(
        method = method,
        madhab = madhab,
        highLatitudeRule = highLatitudeRule,
        elevationMeters = elevationMeters,
        temperatureC = temperatureC,
        pressureMbar = pressureMbar,
        customMethodParams = customMethodParams
    )
}
