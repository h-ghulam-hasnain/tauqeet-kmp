package com.tauqeet.library.internal

import kotlin.math.PI
import kotlin.math.cos

fun linearInterpolation(
    x0: Double,
    y0: Double,
    x1: Double,
    y1: Double,
    x: Double
): Double {
    if (x1 == x0) {
        return y0
    }
    return y0 + ((y1 - y0) * (x - x0)) / (x1 - x0)
}

/**
 * Chebyshev polynomial interpolator using Clenshaw's recurrence for evaluation.
 */
class ChebyshevInterpolator(
    private val a: Double,
    private val b: Double,
    samples: DoubleArray
) {
    /** Chebyshev expansion coefficients c0…c_{n-1}. */
    private val coefficients: DoubleArray
    private val n: Int

    init {
        val n = samples.size
        this.n = n

        // -- Pre-compute the full N×N DCT cosine matrix (done once) --
        val cosMatrix = DoubleArray(n * n)
        val piOver2n = PI / (2.0 * n)
        for (j in 0 until n) {
            val rowBase = j * n
            for (k in 1..n) {
                cosMatrix[rowBase + (k - 1)] = cos(j * (2.0 * k - 1.0) * piOver2n)
            }
        }

        // -- Compute Chebyshev coefficients via DCT-II using Kahan summation --
        val scale = 2.0 / n
        val coefficients = DoubleArray(n)
        for (j in 0 until n) {
            // Inline Kahan compensated sum — O(1) space, no temporary array.
            var sum = 0.0
            var c = 0.0
            val rowBase = j * n
            for (k in 0 until n) {
                val value = samples[k] * cosMatrix[rowBase + k]
                val y = value - c
                val t = sum + y
                c = t - sum - y
                sum = t
            }
            val coeff = scale * sum
            coefficients[j] = if (j == 0) coeff / 2.0 else coeff
        }
        this.coefficients = coefficients
    }

    /**
     * Evaluates the interpolated value at point `u ∈ [a, b]`.
     *
     * Uses Clenshaw's recurrence — numerically stable, O(N), no allocations.
     */
    fun evaluate(u: Double): Double {
        // Map u from [a, b] -> x in [-1, 1]
        val x = (2.0 * u - (this.a + this.b)) / (this.b - this.a)
        val x2 = 2.0 * x

        // Clenshaw's recurrence (backward accumulation)
        var d1 = 0.0
        var d2 = 0.0
        for (k in this.n - 1 downTo 1) {
            val temp = d1
            d1 = x2 * d1 - d2 + this.coefficients[k]
            d2 = temp
        }
        return x * d1 - d2 + this.coefficients[0]
    }
}
