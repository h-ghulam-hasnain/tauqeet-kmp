package com.tauqeet.library.internal

import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

fun sind(x: Double): Double {
    return sin(degreesToRadians(x))
}

fun cosd(x: Double): Double {
    return cos(degreesToRadians(x))
}

fun tand(x: Double): Double {
    return tan(degreesToRadians(x))
}

fun asind(x: Double): Double {
    return radiansToDegrees(asin(x))
}

fun acosd(x: Double): Double {
    return radiansToDegrees(acos(x))
}

fun atand(x: Double): Double {
    return radiansToDegrees(atan(x))
}

fun atand2(x: Double, y: Double): Double {
    return radiansToDegrees(atan2(x, y))
}

fun ahavd(x: Double): Double {
    return radiansToDegrees(2.0 * asin(sqrt(x)))
}
