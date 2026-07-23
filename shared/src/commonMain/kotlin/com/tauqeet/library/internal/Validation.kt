package com.tauqeet.library.internal

import com.tauqeet.library.astronomy.InvalidArgumentError

/**
 * Validates latitude and longitude values, throwing InvalidArgumentError with a
 * descriptive message if a value is out of its legal range.
 */
fun validateCoordinates(latitude: Double, longitude: Double) {
    if (latitude.isNaN()) {
        throw InvalidArgumentError("Latitude must be a number, received: $latitude")
    }
    if (longitude.isNaN()) {
        throw InvalidArgumentError("Longitude must be a number, received: $longitude")
    }
    if (latitude <= -90.0 || latitude >= 90.0) {
        throw InvalidArgumentError("Latitude must be strictly between -90 and 90, received: $latitude")
    }
    if (longitude <= -180.0 || longitude >= 180.0) {
        throw InvalidArgumentError("Longitude must be strictly between -180 and 180, received: $longitude")
    }
}
