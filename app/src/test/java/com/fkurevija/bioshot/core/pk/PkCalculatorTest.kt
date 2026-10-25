package com.fkurevija.bioshot.core.pk

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PkCalculatorTest {

    @Test
    fun testEliminationRateConstant() {
        // halfLife = 168 hours (7 days, typical for Semaglutide)
        val kel = PkCalculator.calculateEliminationRateConstant(168.0)
        val expected = kotlin.math.ln(2.0) / 168.0
        assertEquals(expected, kel, 1e-6)

        // Invalid half-life
        assertEquals(0.0, PkCalculator.calculateEliminationRateConstant(0.0), 1e-6)
        assertEquals(0.0, PkCalculator.calculateEliminationRateConstant(-10.0), 1e-6)
    }

    @Test
    fun testConcentrationBeforeDose() {
        val doseTime = 1000L
        val dose = PkCalculator.DoseEvent(timestamp = doseTime, doseAmount = 40.0)
        // Time before dose
        val conc = PkCalculator.calculateConcentrationAtTime(
            timeMs = 500L,
            dose = dose,
            halfLifeHours = 168.0
        )
        assertEquals(0.0, conc, 1e-6)
    }

    @Test
    fun testConcentrationAfterDose() {
        val doseTime = 0L
        val dose = PkCalculator.DoseEvent(timestamp = doseTime, doseAmount = 40.0, bioavailability = 1.0)
        
        // 24 hours later
        val time24h = 24L * 60L * 60L * 1000L
        val conc24h = PkCalculator.calculateConcentrationAtTime(
            timeMs = time24h,
            dose = dose,
            halfLifeHours = 168.0,
            absorptionHalfLifeHours = 24.0
        )
        assertTrue(conc24h > 0.0)
    }

    @Test
    fun testGenerateCurve() {
        val doseTime = System.currentTimeMillis()
        val doses = listOf(
            PkCalculator.DoseEvent(timestamp = doseTime, doseAmount = 40.0)
        )
        val curve = PkCalculator.generateCurve(
            doses = doses,
            startTimeMs = doseTime,
            endTimeMs = doseTime + 7L * 24L * 60L * 60L * 1000L,
            stepCount = 10,
            halfLifeHours = 168.0
        )
        assertEquals(11, curve.size)
        for (point in curve) {
            assertTrue(point.concentration >= 0.0)
        }
    }
}
