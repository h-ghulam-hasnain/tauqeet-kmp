package com.tauqeet.library

import kotlin.math.roundToInt

/**
 * Converts a time represented as milliseconds since midnight into a "HH:mm" formatted string.
 */
fun Long.toTimeString(): String {
    val totalSeconds = (this / 1000.0).roundToInt()
    val totalMinutes = totalSeconds / 60
    val normalizedMinutes = ((totalMinutes % 1440) + 1440) % 1440
    val hours = normalizedMinutes / 60
    val minutes = normalizedMinutes % 60
    
    val hStr = if (hours < 10) "0$hours" else hours.toString()
    val mStr = if (minutes < 10) "0$minutes" else minutes.toString()
    return "$hStr:$mStr"
}
