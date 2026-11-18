package com.tauqeet.library.prayers

import com.tauqeet.library.DateComponents
import com.tauqeet.library.TauqeetException
import com.tauqeet.library.validateDate
import com.tauqeet.library.validateLatitude
import com.tauqeet.library.validateLongitude
import com.tauqeet.library.validateTimezoneOffset

/**
 * Rounding strategy applied to raw millisecond prayer times before they are returned to the caller.
 *
 * @property FIQH_IHTIYAT  Conservative (Fiqh Ihtiyat) rounding: Fajr and Sunrise are rounded
 *                          **down** (earlier), all other prayers are rounded **up** (later).
 *                          This adds a small safety margin in accordance with traditional Fiqh caution.
 * @property NEAREST        Standard rounding to the nearest second. No safety bias.
 */
enum class RoundingStrategy {
    FIQH_IHTIYAT,
    NEAREST
}

/**
 * Encapsulates the full set of calculation parameters used for a single prayer-time computation.
 *
 * Typically obtained from the `calculation { }` block inside `prayerRequest { }` rather than
 * constructed directly.
 *
 * @property method              The named calculation authority. Default: [CalculationMethod.KARACHI].
 * @property madhab              Juristic school that determines the Asr shadow multiplier. Default: [Madhab.HANAFI].
 * @property highLatitudeRule    Fallback strategy for high-latitude locations. Default: [HighLatitudeRule.MIDDLE_OF_NIGHT].
 * @property elevationMeters     Observer's altitude above sea level in metres. Default: 0.0.
 * @property temperatureC        Ambient temperature in Celsius for refraction correction. Default: 10.0.
 * @property pressureMbar        Atmospheric pressure in millibars for refraction correction. Default: 1010.0.
 * @property customMethodParams  When non-null, overrides [method]'s angles/intervals entirely. Default: null.
 * @property roundingStrategy    Rounding convention applied to millisecond results. Default: [RoundingStrategy.FIQH_IHTIYAT].
 */
data class PrayerCalculationParameters(
    val method: CalculationMethod = CalculationMethod.KARACHI,
    val madhab: Madhab = Madhab.HANAFI,
    val highLatitudeRule: HighLatitudeRule = HighLatitudeRule.MIDDLE_OF_NIGHT,
    val elevationMeters: Double = 0.0,
    val temperatureC: Double = 10.0,
    val pressureMbar: Double = 1010.0,
    val customMethodParams: CalculationMethodParameters? = null,
    val roundingStrategy: RoundingStrategy = RoundingStrategy.FIQH_IHTIYAT
)

/**
 * Immutable, validated request object for a single prayer-time computation.
 *
 * The primary constructor is `internal` — always create instances using the DSL:
 * ```kotlin
 * val request = prayerRequest {
 *     latitude = 51.5; longitude = -0.1
 *     date = DateComponents(2026, 6, 15)
 *     timeZoneOffset = 1.0
 *     calculation { method = CalculationMethod.MWL }
 * }
 * ```
 *
 * @property latitude              Observer's latitude in decimal degrees (−90.0 to 90.0).
 * @property longitude             Observer's longitude in decimal degrees (−180.0 to 180.0).
 * @property date                  Gregorian date as a [com.tauqeet.library.DateComponents].
 * @property timeZoneOffset        Hours offset from UTC (−12.0 to +14.0). Use `0.0` for UTC.
 * @property calculationParameters The full set of calculation parameters (method, madhab, etc.).
 * @property includeAdvancedMetadata  When `true`, the result includes per-prayer VSOP87 diagnostics.
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
 * Type-safe DSL entry point for constructing a validated [PrayerRequest].
 *
 * The [builder] lambda is applied to a fresh [PrayerRequestBuilder]; [PrayerRequestBuilder.build]
 * is then called automatically, running all validation before the [PrayerRequest] is returned.
 *
 * Example:
 * ```kotlin
 * val request = prayerRequest {
 *     latitude = 24.4539; longitude = 54.3773
 *     date = DateComponents(2026, 11, 18)
 *     timeZoneOffset = 4.0
 *     calculation {
 *         method = CalculationMethod.DUBAI
 *         madhab = Madhab.HANAFI
 *     }
 * }
 * ```
 *
 * @param builder  Configuration lambda applied to [PrayerRequestBuilder].
 * @return A fully validated, immutable [PrayerRequest].
 * @throws [com.tauqeet.library.TauqeetException] if any value set inside the builder is out of range.
 */
@Throws(TauqeetException::class)
inline fun prayerRequest(builder: PrayerRequestBuilder.() -> Unit): PrayerRequest =
    PrayerRequestBuilder().apply(builder).build()

/**
 * Mutable builder used by the [prayerRequest] DSL.
 *
 * All fields have safe defaults. Set at minimum [latitude], [longitude], and [date] before
 * calling [build]. Use the nested [calculation] block to configure the calculation authority.
 *
 * @property latitude              Observer's latitude in decimal degrees. **Required** (no valid default).
 * @property longitude             Observer's longitude in decimal degrees. **Required** (no valid default).
 * @property date                  Gregorian date. Default: 1970-01-01.
 * @property timeZoneOffset        Hours offset from UTC. Default: 0.0 (UTC).
 * @property calculation           Nested builder for calculation parameters. Default: KARACHI/HANAFI/MIDDLE_OF_NIGHT.
 * @property includeAdvancedMetadata  When `true`, the result includes per-prayer VSOP87 diagnostics. Default: false.
 */
class PrayerRequestBuilder {
    var latitude: Double = Double.NaN
    var longitude: Double = Double.NaN
    var date: DateComponents = DateComponents(1970, 1, 1)
    var timeZoneOffset: Double = 0.0
    var calculation: PrayerCalculationParametersBuilder = PrayerCalculationParametersBuilder()
    var includeAdvancedMetadata: Boolean = false

    /**
     * Configures the calculation parameters via a nested DSL block.
     *
     * @param builder  Lambda applied to [PrayerCalculationParametersBuilder].
     */
    fun calculation(builder: PrayerCalculationParametersBuilder.() -> Unit) {
        calculation.apply(builder)
    }

    /**
     * Validates all fields and constructs an immutable [PrayerRequest].
     *
     * @return A fully validated [PrayerRequest].
     * @throws [com.tauqeet.library.TauqeetException] if [latitude], [longitude], [timeZoneOffset],
     *         or [date] components are outside their valid ranges.
     */
    @Throws(TauqeetException::class)
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
 * Nested builder for configuring the calculation authority, madhab, high-latitude rule,
 * and environmental parameters.
 *
 * @property method              Named calculation authority. Default: [CalculationMethod.KARACHI].
 * @property madhab              Juristic school for Asr shadow length. Default: [Madhab.HANAFI].
 * @property highLatitudeRule    High-latitude estimation rule. Default: [HighLatitudeRule.MIDDLE_OF_NIGHT].
 * @property elevationMeters     Observer's altitude in metres. Default: 0.0.
 * @property temperatureC        Temperature in Celsius for refraction. Default: 10.0.
 * @property pressureMbar        Pressure in millibars for refraction. Default: 1010.0.
 * @property customMethodParams  Fully custom angle/interval override. When non-null, [method] angles are ignored. Default: null.
 * @property roundingStrategy    Rounding convention. Default: [RoundingStrategy.FIQH_IHTIYAT].
 */
class PrayerCalculationParametersBuilder {
    var method: CalculationMethod = CalculationMethod.KARACHI
    var madhab: Madhab = Madhab.HANAFI
    var highLatitudeRule: HighLatitudeRule = HighLatitudeRule.MIDDLE_OF_NIGHT
    var elevationMeters: Double = 0.0
    var temperatureC: Double = 10.0
    var pressureMbar: Double = 1010.0
    var customMethodParams: CalculationMethodParameters? = null
    var roundingStrategy: RoundingStrategy = RoundingStrategy.FIQH_IHTIYAT

    /** Builds an immutable [PrayerCalculationParameters] snapshot from the current builder state. */
    fun toParams(): PrayerCalculationParameters = PrayerCalculationParameters(
        method = method,
        madhab = madhab,
        highLatitudeRule = highLatitudeRule,
        elevationMeters = elevationMeters,
        temperatureC = temperatureC,
        pressureMbar = pressureMbar,
        customMethodParams = customMethodParams,
        roundingStrategy = roundingStrategy
    )
}
