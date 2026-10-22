package com.fkurevija.bioshot.core.model

import kotlinx.serialization.Serializable

/**
 * Represents a drug (active ingredient) that can be selected for a medication.
 * This is distinct from a Medication, which pairs a drug with a dosage schedule and device.
 */
@Serializable
data class Drug(
    val id: Long = 0L,
    val name: String,
    val genericName: String? = null,
    val manufacturer: String? = null,
    val ndcCode: String? = null, // National Drug Code (US) or equivalent identifier
    val createdAt: Long = System.currentTimeMillis()
)
