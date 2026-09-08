package com.tauqeet.library.prayers

import com.tauqeet.library.toTimeString
import com.tauqeet.library.toTimeStringShort
import kotlinx.serialization.Serializable

@Serializable
data class NextPrayerInfo(
    val name: String,
    val timestampMs: Long,
    val timeRemainingMs: Long,
    val minutesUntil: Long,
    val formattedRemaining: String
)

/**
 * Formats a prayer time (in ms since midnight) into a time string.
 */
fun Long.formatTime(pattern: String = "HH:mm"): String {
    val totalSeconds = (this / 1000.0).let { if (it < 0) 0.0 else it }.toLong()
    val normalizedSeconds = ((totalSeconds % 86400) + 86400) % 86400
    val hours24 = (normalizedSeconds / 3600).toInt()
    val minutes = ((normalizedSeconds % 3600) / 60).toInt()
    val seconds = (normalizedSeconds % 60).toInt()

    val pad = { n: Int -> if (n < 10) "0$n" else n.toString() }
    val hours12 = if (hours24 % 12 == 0) 12 else hours24 % 12
    val ampm = if (hours24 < 12) "AM" else "PM"

    return when (pattern.lowercase()) {
        "12h", "hh:mm a" -> "${pad(hours12)}:${pad(minutes)} $ampm"
        "hh:mm:ss a" -> "${pad(hours12)}:${pad(minutes)}:${pad(seconds)} $ampm"
        "24h", "hh:mm" -> "${pad(hours24)}:${pad(minutes)}"
        "hh:mm:ss" -> "${pad(hours24)}:${pad(minutes)}:${pad(seconds)}"
        else -> "${pad(hours24)}:${pad(minutes)}"
    }
}

/**
 * Formats all calculated prayer times in [PrayerTimesResult] into a map of prayer name -> formatted string.
 */
fun PrayerTimesResult.format(pattern: String = "HH:mm"): Map<String, String?> {
    return mapOf(
        "fajr" to fajr?.formatTime(pattern),
        "sunrise" to sunrise?.formatTime(pattern),
        "dhahwaKubra" to dhahwaKubra?.formatTime(pattern),
        "dhuhr" to dhuhr?.formatTime(pattern),
        "asr" to asr?.formatTime(pattern),
        "maghrib" to maghrib?.formatTime(pattern),
        "isha" to isha?.formatTime(pattern)
    )
}

/**
 * Identifies the upcoming next prayer from a [PrayerTimesResult] given [nowMs] (ms since midnight).
 */
fun PrayerTimesResult.getNextPrayer(nowMs: Long): NextPrayerInfo? {
    val msInDay = 86400000L
    val normalizedNow = ((nowMs % msInDay) + msInDay) % msInDay

    val prayers = listOf(
        "fajr" to fajr,
        "sunrise" to sunrise,
        "dhuhr" to dhuhr,
        "asr" to asr,
        "maghrib" to maghrib,
        "isha" to isha
    )

    for ((name, timeMs) in prayers) {
        if (timeMs != null && timeMs > normalizedNow) {
            val remainingMs = timeMs - normalizedNow
            val minutesUntil = remainingMs / 60000L
            val hoursUntil = minutesUntil / 60L
            val minsRem = minutesUntil % 60L
            val formattedRemaining = "${hoursUntil}h ${minsRem}m"

            return NextPrayerInfo(
                name = name,
                timestampMs = timeMs,
                timeRemainingMs = remainingMs,
                minutesUntil = minutesUntil,
                formattedRemaining = formattedRemaining
            )
        }
    }

    // If past Isha, next prayer is Fajr tomorrow
    if (fajr != null) {
        val remainingMs = (msInDay - normalizedNow) + fajr
        val minutesUntil = remainingMs / 60000L
        val hoursUntil = minutesUntil / 60L
        val minsRem = minutesUntil % 60L

        return NextPrayerInfo(
            name = "fajr",
            timestampMs = fajr,
            timeRemainingMs = remainingMs,
            minutesUntil = minutesUntil,
            formattedRemaining = "${hoursUntil}h ${minsRem}m"
        )
    }

    return null
}

/**
 * Calculates remaining time until the next prayer.
 */
fun PrayerTimesResult.getTimeRemaining(nowMs: Long): NextPrayerInfo? {
    return getNextPrayer(nowMs)
}
