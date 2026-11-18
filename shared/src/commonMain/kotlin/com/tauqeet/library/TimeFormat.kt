package com.tauqeet.library

import kotlin.math.roundToInt

/**
 * Formats this [Long] millisecond-since-midnight value as a `HH:mm:ss` string.
 *
 * Values outside the range 0–86 399 999 ms are normalised (wrapped) to fit within a single day.
 *
 * @receiver Milliseconds since UTC midnight.
 * @return A zero-padded 24-hour time string, e.g. `"05:32:07"` or `"23:59:59"`.
 */
fun Long.toTimeString(): String {
    val totalSeconds = (this / 1000.0).roundToInt()
    val normalizedSeconds = ((totalSeconds % 86400) + 86400) % 86400
    val hours = normalizedSeconds / 3600
    val minutes = (normalizedSeconds % 3600) / 60
    val seconds = normalizedSeconds % 60

    val hStr = if (hours < 10) "0$hours" else hours.toString()
    val mStr = if (minutes < 10) "0$minutes" else minutes.toString()
    val sStr = if (seconds < 10) "0$seconds" else seconds.toString()
    return "$hStr:$mStr:$sStr"
}

/**
 * Formats this [Long] millisecond-since-midnight value as a `HH:mm` string (seconds omitted).
 *
 * Values outside the range 0–86 399 999 ms are normalised (wrapped) to fit within a single day.
 *
 * @receiver Milliseconds since UTC midnight.
 * @return A zero-padded 24-hour time string without seconds, e.g. `"05:32"`.
 */
fun Long.toTimeStringShort(): String {
    val totalSeconds = (this / 1000.0).roundToInt()
    val normalizedSeconds = ((totalSeconds % 86400) + 86400) % 86400
    val hours = normalizedSeconds / 3600
    val minutes = (normalizedSeconds % 3600) / 60

    val hStr = if (hours < 10) "0$hours" else hours.toString()
    val mStr = if (minutes < 10) "0$minutes" else minutes.toString()
    return "$hStr:$mStr"
}

/**
 * Formats this [Long] millisecond-since-midnight value as an ISO 8601 time string (`HH:mm:ss`).
 *
 * Delegates to [toTimeString]. Provided for consistency with JS library behaviour where
 * `Date` objects produce `HH:mm:ss` by default.
 *
 * @receiver Milliseconds since UTC midnight.
 * @return A zero-padded ISO 8601 time string, e.g. `"04:32:00"`.
 */
fun Long.toISOTimeString(): String {
    return this.toTimeString()
}

/**
 * Formats this [Long] millisecond-since-midnight value as an ISO 8601 time string with
 * millisecond precision (`HH:mm:ss.SSS`).
 *
 * Values outside the range 0–86 399 999 ms are normalised (wrapped) to fit within a single day.
 *
 * @receiver Milliseconds since UTC midnight.
 * @return A zero-padded time string with milliseconds, e.g. `"04:32:00.750"`.
 */
fun Long.toISOTimeStringWithMillis(): String {
    val normalizedMs = ((this % 86400000L) + 86400000L) % 86400000L
    val hours = normalizedMs / 3600000L
    val minutes = (normalizedMs % 3600000L) / 60000L
    val seconds = (normalizedMs % 60000L) / 1000L
    val millis = normalizedMs % 1000L

    val hStr = if (hours < 10) "0$hours" else hours.toString()
    val mStr = if (minutes < 10) "0$minutes" else minutes.toString()
    val sStr = if (seconds < 10) "0$seconds" else seconds.toString()
    val msStr = if (millis < 10) "00$millis" else if (millis < 100) "0$millis" else millis.toString()
    
    return "$hStr:$mStr:$sStr.$msStr"
}
