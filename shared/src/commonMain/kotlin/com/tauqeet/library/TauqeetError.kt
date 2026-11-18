package com.tauqeet.library

/**
 * Enumeration of all structured error codes that [TauqeetException] can carry.
 *
 * Use these codes for programmatic error handling instead of parsing the exception message string.
 *
 * @property INVALID_LATITUDE          The supplied latitude was outside the range −90.0 to 90.0.
 * @property INVALID_LONGITUDE         The supplied longitude was outside the range −180.0 to 180.0.
 * @property INVALID_DATE              The supplied year, month, or day was out of the valid range.
 * @property INVALID_TIMEZONE_OFFSET   The UTC offset was outside the range −12.0 to +14.0.
 * @property INVALID_METHOD            An unrecognised or incompatible calculation method was supplied.
 * @property INVALID_CONFIG            A constructor-level environmental parameter (elevation,
 *                                     temperature, or pressure) was outside its valid range.
 * @property UNRESOLVED_EXTREME_LATITUDE  The location is at a latitude so extreme that prayer
 *                                     times cannot be resolved even after applying the configured
 *                                     [HighLatitudeRule][com.tauqeet.library.prayers.HighLatitudeRule].
 */
enum class TauqeetErrorCode {
    INVALID_LATITUDE,
    INVALID_LONGITUDE,
    INVALID_DATE,
    INVALID_TIMEZONE_OFFSET,
    INVALID_METHOD,
    INVALID_CONFIG,
    UNRESOLVED_EXTREME_LATITUDE
}

/**
 * Exception thrown by any Tauqeet API that validates its inputs.
 *
 * Always carries a machine-readable [errorCode] alongside the human-readable [message],
 * allowing callers to react programmatically without parsing strings.
 *
 * ```kotlin
 * try {
 *     tauqeet.computePrayerTimes(request)
 * } catch (e: TauqeetException) {
 *     when (e.errorCode) {
 *         TauqeetErrorCode.INVALID_LATITUDE -> handleBadLat()
 *         else -> showGenericError(e.message)
 *     }
 * }
 * ```
 *
 * Prefer [SafeResult] / [com.tauqeet.library.Tauqeet.computePrayerTimesSafe] to avoid try/catch entirely.
 *
 * @param errorCode  Structured code identifying the category of validation failure.
 * @param message    Human-readable description of what was invalid and what the valid range is.
 * @param cause      Optional upstream throwable if this exception wraps another error.
 */
open class TauqeetException(
    val errorCode: TauqeetErrorCode,
    message: String,
    cause: Throwable? = null
) : IllegalArgumentException(message, cause)

/**
 * A discriminated-union result type returned by the safe variants of the Tauqeet API
 * (e.g., [com.tauqeet.library.Tauqeet.computePrayerTimesSafe]).
 *
 * Use pattern-matching on [Success] and [Failure] instead of try/catch:
 *
 * ```kotlin
 * when (val result = tauqeet.computePrayerTimesSafe(request)) {
 *     is SafeResult.Success -> display(result.data)
 *     is SafeResult.Failure -> showError(result.message)
 * }
 * ```
 *
 * Helper accessors [isSuccess], [isFailure], [getOrNull], and [exceptionOrNull] are also available
 * for cases where exhaustive matching is not needed.
 */
sealed interface SafeResult<out T> {
    /** Represents a successful computation. Contains the result [data]. */
    data class Success<out T>(val data: T) : SafeResult<T>

    /**
     * Represents a validation failure. Contains the structured [errorCode], a human-readable
     * [message], and an optional [cause] throwable.
     *
     * @property errorCode  The [TauqeetErrorCode] identifying the category of failure.
     * @property message    Human-readable description of the validation error.
     * @property cause      The underlying [TauqeetException], if available.
     */
    data class Failure(
        val errorCode: TauqeetErrorCode,
        val message: String,
        val cause: Throwable? = null
    ) : SafeResult<Nothing>

    /** `true` if this result is a [Success]. */
    val isSuccess: Boolean get() = this is Success

    /** `true` if this result is a [Failure]. */
    val isFailure: Boolean get() = this is Failure

    /** Returns the [Success.data] value, or `null` if this is a [Failure]. */
    fun getOrNull(): T? = (this as? Success)?.data

    /** Reconstructs a [TauqeetException] from a [Failure], or returns `null` for a [Success]. */
    fun exceptionOrNull(): Throwable? = (this as? Failure)?.let { TauqeetException(it.errorCode, it.message, it.cause) }
}
