package com.fkurevija.bioshot.core.pk

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PkCalculatorTest {

    @Test
    fun testEliminationRateConstant() {
        val halfLifeHours = 168.0 // 1 week
        val kel = PkCalculator.calculateEliminationRateConstant(halfLifeHours)
        assertTrue(kel > 0.0)
        // kel should be ln(2) / 168
        val expected = kotlin.math.ln(2.0) / 168.0
        assertEquals(expected, kel, 1e-6)
    }

    @Test
    fun testZeroConcentrationBeforeDose() {
        val doseTime = 1000L
        val dose = PkCalculator.DoseEvent(timestamp = doseTime, doseAmount = 100.0)
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
        val dose = PkCalculator.DoseEvent(timestamp = doseTime, doseAmount = 100.0, bioavailability = 1.0)
        
        // Evaluate at 24 hours later
        val time24h = 24L * 60L * 60L * 1000L
        val conc = PkCalculator.calculateConcentrationAtTime(
            timeMs = time24h,
            dose = dose,
            halfLifeHours = 168.0,
            absorptionHalfLifeHours = 24.0
        )

        assertTrue(conc > 0.0)
    }

    @Test
    fun testGenerateCurve() {
        val doseTime = 0L
        val doses = listOf(
            PkCalculator.DoseEvent(timestamp = doseTime, doseAmount = 50.0)
        )

        val endTime = 168L * 60L * 60L * 1000L // 1 week later
        val curve = PkCalculator.generateCurve(
            doses = doses,
            startTimeMs = 0L,
            endTimeMs = endTime,
            stepCount = 10,
            halfLifeHours = 168.0
        )

        assertEquals(11, curve.size) // 0 to 10 inclusive = 11 points
        assertNotNull(PkCalculator.DISCLAIMER_TEXT)
        assertTrue(PkCalculator.DISCLAIMER_TEXT.contains("ESTIMATE ONLY"))
    }
}
