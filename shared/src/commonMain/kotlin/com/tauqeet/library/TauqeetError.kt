package com.tauqeet.library

enum class TauqeetErrorCode {
    INVALID_LATITUDE,
    INVALID_LONGITUDE,
    INVALID_DATE,
    INVALID_TIMEZONE_OFFSET,
    INVALID_METHOD,
    INVALID_CONFIG,
    UNRESOLVED_EXTREME_LATITUDE
}

open class TauqeetException(
    val errorCode: TauqeetErrorCode,
    message: String,
    cause: Throwable? = null
) : IllegalArgumentException(message, cause)

sealed interface SafeResult<out T> {
    data class Success<out T>(val data: T) : SafeResult<T>
    data class Failure(
        val errorCode: TauqeetErrorCode,
        val message: String,
        val cause: Throwable? = null
    ) : SafeResult<Nothing>

    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is Failure

    fun getOrNull(): T? = (this as? Success)?.data
    fun exceptionOrNull(): Throwable? = (this as? Failure)?.let { TauqeetException(it.errorCode, it.message, it.cause) }
}
