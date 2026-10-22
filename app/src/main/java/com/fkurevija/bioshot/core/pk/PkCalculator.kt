package com.fkurevija.bioshot.core.pk

import kotlin.math.exp
import kotlin.math.ln

/**
 * PkCalculator provides one-compartment, first-order absorption and elimination pharmacokinetic (PK)
 * modeling to estimate serum medication concentration over time based on drug half-life, bioavailability,
 * dose history, and absorption rate constant.
 *
 * DISCLAIMER:
 * ESTIMATE ONLY. This pharmacokinetic calculation tool is intended solely for informational and
 * tracking purposes. It does not constitute medical advice, diagnosis, or treatment. Serum concentration
 * curves are theoretical mathematical estimates and may not reflect actual physiological levels. Consult
 * a qualified healthcare provider for medical decisions.
 */
object PkCalculator {

    const val DISCLAIMER_TEXT = "ESTIMATE ONLY: Pharmacokinetic concentrations are mathematical models " +
            "and do not substitute for professional medical advice or laboratory blood tests."

    /**
     * Represents a single dose administration event in history.
     * @param timestamp Time of administration (milliseconds or arbitrary epoch).
     * @param doseAmount Numeric dose amount (e.g. mg).
     * @param bioavailability Fractional bioavailability (0.0 to 1.0, default 1.0).
     */
    data class DoseEvent(
        val timestamp: Long,
        val doseAmount: Double,
        val bioavailability: Double = 1.0
    )

    /**
     * Represents a calculated concentration data point on the PK curve.
     * @param timestamp Time point (milliseconds).
     * @param concentration Estimated serum concentration (e.g., mg/L or arbitrary units).
     */
    data class PkDataPoint(
        val timestamp: Long,
        val concentration: Double
    )

    /**
     * Calculates the elimination rate constant (kel) from half-life.
     * kel = ln(2) / halfLife
     *
     * @param halfLifeHours Half-life of the drug in hours.
     * @return Elimination rate constant (1/hours).
     */
    fun calculateEliminationRateConstant(halfLifeHours: Double): Double {
        if (halfLifeHours <= 0.0) return 0.0
        return ln(2.0) / halfLifeHours
    }

    /**
     * Calculates serum concentration at a specific time t given a single dose event,
     * using a one-compartment model with first-order absorption and elimination.
     *
     * C(t) = [ (F * Dose * ka) / (Vd * (ka - kel)) ] * [ exp(-kel * (t - t_dose)) - exp(-ka * (t - t_dose)) ]
     *
     * For simplicity of tracking relative trends where Volume of Distribution (Vd) is normalized to 1.0:
     * C(t) = [ (F * Dose * ka) / (ka - kel) ] * [ exp(-kel * dt) - exp(-ka * dt) ]
     * If ka approx kel, limit or standard approximation can be applied.
     *
     * @param timeMs Target time evaluation point (milliseconds).
     * @param dose The [DoseEvent].
     * @param halfLifeHours Half-life in hours.
     * @param absorptionHalfLifeHours Absorption half-life in hours (default 24 hours / 1 day).
     * @param volumeOfDistribution Normalized or absolute volume of distribution (default 1.0 for relative curves).
     * @return Estimated concentration at timeMs.
     */
    fun calculateConcentrationAtTime(
        timeMs: Long,
        dose: DoseEvent,
        halfLifeHours: Double,
        absorptionHalfLifeHours: Double = 24.0,
        volumeOfDistribution: Double = 1.0
    ): Double {
        if (timeMs <= dose.timestamp) return 0.0

        val dtHours = (timeMs - dose.timestamp).toDouble() / (1000.0 * 60.0 * 60.0)
        if (dtHours < 0.0) return 0.0

        val kel = calculateEliminationRateConstant(halfLifeHours)
        val ka = calculateEliminationRateConstant(absorptionHalfLifeHours)

        if (kel <= 0.0 || ka <= 0.0 || volumeOfDistribution <= 0.0) return 0.0

        // Handle edge case where ka == kel
        val concentration = if (kotlin.math.abs(ka - kel) < 1e-6) {
            // Bateman equation limit when ka -> kel: C(t) = (F * Dose / Vd) * ka * dt * exp(-ka * dt)
            (dose.bioavailability * dose.doseAmount * ka / volumeOfDistribution) *
                    dtHours * exp(-ka * dtHours)
        } else {
            val factor = (dose.bioavailability * dose.doseAmount * ka) / (volumeOfDistribution * (ka - kel))
            factor * (exp(-kel * dtHours) - exp(-ka * dtHours))
        }

        return maxOf(0.0, concentration)
    }

    /**
     * Generates a complete pharmacokinetic curve over a time range given a history of doses.
     *
     * @param doses List of past and scheduled [DoseEvent]s.
     * @param startTimeMs Start timestamp of the curve.
     * @param endTimeMs End timestamp of the curve.
     * @param stepCount Number of data points to generate across the range (default 100).
     * @param halfLifeHours Drug elimination half-life in hours.
     * @param absorptionHalfLifeHours Drug absorption half-life in hours (default 24.0).
     * @param volumeOfDistribution Volume of distribution (default 1.0).
     * @return List of [PkDataPoint] representing the estimated serum concentration over time.
     */
    fun generateCurve(
        doses: List<DoseEvent>,
        startTimeMs: Long,
        endTimeMs: Long,
        stepCount: Int = 100,
        halfLifeHours: Double,
        absorptionHalfLifeHours: Double = 24.0,
        volumeOfDistribution: Double = 1.0
    ): List<PkDataPoint> {
        if (startTimeMs >= endTimeMs || stepCount <= 0 || doses.isEmpty()) return emptyList()

        val stepSize = (endTimeMs - startTimeMs).toDouble() / stepCount.toDouble()
        val curve = mutableListOf<PkDataPoint>()

        for (i in 0..stepCount) {
            val t = startTimeMs + (i * stepSize).toLong()
            val totalConcentration = doses.sumOf { dose ->
                calculateConcentrationAtTime(
                    timeMs = t,
                    dose = dose,
                    halfLifeHours = halfLifeHours,
                    absorptionHalfLifeHours = absorptionHalfLifeHours,
                    volumeOfDistribution = volumeOfDistribution
                )
            }
            curve.add(PkDataPoint(timestamp = t, concentration = totalConcentration))
        }

        return curve
    }
}
