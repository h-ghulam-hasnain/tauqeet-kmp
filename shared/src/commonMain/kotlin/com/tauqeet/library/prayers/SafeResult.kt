package com.tauqeet.library.prayers

sealed class SafeResult<out T> {
    data class Success<out T>(val data: T) : SafeResult<T>()
    data class Failure(
        val code: String, 
        val message: String, 
        val cause: Throwable? = null
    ) : SafeResult<Nothing>()
}
