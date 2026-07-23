package com.tauqeet.library.internal

import kotlin.math.min

fun dot(a: DoubleArray, b: DoubleArray): Double {
    var result = 0.0
    val len = min(a.size, b.size)
    for (i in 0 until len) {
        val ai = a[i]
        val bi = b[i]
        result += ai * bi
    }
    return result
}
