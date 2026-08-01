package com.tauqeet.library

import com.tauqeet.library.astronomy.computeEarthHeliocentricState
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.measureTime

class TauqeetBenchmarkTest {

    @Test
    fun benchmarkCorePrayerTimeCalculation() {
        val tauqeet = Tauqeet()
        
        // Warmup
        for (i in 0..10) {
            tauqeet.computePrayerTimes(2026, 8, 1, 24.8607, 67.0011, 5.0)
        }
        
        // Benchmark
        val duration = measureTime {
            for (i in 0..1000) {
                tauqeet.computePrayerTimes(2026, 8, 1, 24.8607, 67.0011, 5.0)
            }
        }
        
        // 1000 calculations should be reasonable, relaxing to 10 seconds for CI.
        assertTrue(duration.inWholeMilliseconds < 10000, "Core prayer calculation is too slow: $duration")
    }

    @Test
    fun benchmarkHighFrequencySolarCoordinateEvaluations() {
        // Warmup
        for (i in 0..10) {
            computeEarthHeliocentricState(0.12345)
        }
        
        // Benchmark
        val duration = measureTime {
            for (i in 0..10000) {
                computeEarthHeliocentricState(0.12345)
            }
        }
        
        // VSOP87 is highly optimized, relaxing to 5 seconds for CI.
        assertTrue(duration.inWholeMilliseconds < 5000, "VSOP87 calculation is too slow: $duration")
    }

    @Test
    fun benchmarkGeodesicQibla() {
        val tauqeet = Tauqeet()
        
        // Warmup
        for (i in 0..10) {
            tauqeet.qiblaDirection(24.8607, 67.0011)
        }
        
        // Benchmark
        val duration = measureTime {
            for (i in 0..10000) {
                tauqeet.qiblaDirection(24.8607, 67.0011)
            }
        }
        
        assertTrue(duration.inWholeMilliseconds < 2000, "Qibla calculation is too slow: $duration")
    }
}
