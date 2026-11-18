package com.tauqeet.library.prayers

/**
 * Strategy used to estimate Fajr and Isha times at high latitudes where the sun stays
 * near the horizon for extended periods, making twilight-angle-based calculation unreliable.
 *
 * Apply via [com.tauqeet.library.Tauqeet] constructor or the `calculation { highLatitudeRule = … }`
 * block inside `prayerRequest { }`.
 */
enum class HighLatitudeRule {
    /** Fajr and Isha are set at the midpoint between Dhuhr and the following/preceding midnight. */
    MIDDLE_OF_NIGHT,

    /** Fajr and Isha are set no earlier/later than one-seventh of the night from midnight. */
    SEVENTH_OF_NIGHT,

    /** The twilight angle is used as a fraction of the night length to estimate the time. */
    ANGLE_BASED,

    /** Fajr and Isha are set at the equivalent prayer time for the nearest latitude below 48°. */
    NEAREST_LATITUDE,

    /** No high-latitude adjustment is applied; raw astronomical results are used as-is. */
    NONE
}
