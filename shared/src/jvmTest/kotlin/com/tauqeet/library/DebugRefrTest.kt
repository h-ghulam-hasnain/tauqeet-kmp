package com.tauqeet.library

import com.tauqeet.library.prayers.getRefractionDegrees
import org.junit.Test

class DebugRefrTest {
    @Test
    fun testRefr() {
        val r = getRefractionDegrees(0.0, 10.0, 1010.0)
        println("KMP refraction0: \$r")
    }
}
