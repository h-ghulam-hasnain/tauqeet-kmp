package com.tauqeet.library.internal

fun kahanSum(values: DoubleArray): Double {
    var sum = 0.0
    var c = 0.0
    for (value in values) {
        val y = value - c
        val t = sum + y
        c = t - sum - y
        sum = t
    }
    return sum
}
