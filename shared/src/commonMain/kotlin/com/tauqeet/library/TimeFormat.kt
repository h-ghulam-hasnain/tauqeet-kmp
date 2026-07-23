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
