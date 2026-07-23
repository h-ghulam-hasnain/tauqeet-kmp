package com.tauqeet.library

import kotlin.math.roundToInt

/**
 * Converts a time represented as milliseconds since midnight into a "HH:mm:ss" formatted string.
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
 * Converts a time represented as milliseconds since midnight into a "HH:mm" formatted string (no seconds).
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
 * Converts a time represented as milliseconds since midnight into a "HH:mm:ss" formatted string.
 * This matches the default JS library's rounding behavior for time-only strings.
 */
fun Long.toISOTimeString(): String {
    return this.toTimeString()
}

/**
 * Converts a time represented as milliseconds since midnight into a "HH:mm:ss.SSS" formatted string.
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
