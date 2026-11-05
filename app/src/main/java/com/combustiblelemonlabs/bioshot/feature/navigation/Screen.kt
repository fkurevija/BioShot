package com.combustiblelemonlabs.bioshot.feature.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed class Screen : NavKey {
    @Serializable
    data object Dashboard : Screen()

    @Serializable
    data object MedicationList : Screen()

    @Serializable
    data object MedicationSetupWizard : Screen()

    @Serializable
    data object InjectionLog : Screen()

    @Serializable
    data object SymptomDiary : Screen()

    @Serializable
    data object PkChart : Screen()

    @Serializable
    data object DoctorExportAndSettings : Screen()
}
