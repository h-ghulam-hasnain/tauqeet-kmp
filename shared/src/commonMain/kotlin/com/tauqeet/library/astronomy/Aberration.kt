package com.tauqeet.library.astronomy

/**
 * Computes the effect of solar aberration on the apparent position of the Sun.
 */
fun computeSolarAberration(distanceAu: Double): Double {
    return -SUN_ABERRATION_SECONDS / distanceAu / 3600.0
}
