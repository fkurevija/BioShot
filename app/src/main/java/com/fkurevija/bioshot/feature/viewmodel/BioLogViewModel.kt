package com.fkurevija.bioshot.feature.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fkurevija.bioshot.core.database.BioShotDatabase
import com.fkurevija.bioshot.core.database.DatabaseMigrationException
import com.fkurevija.bioshot.core.database.InjectionLogEntity
import com.fkurevija.bioshot.core.database.MedicationEntity
import com.fkurevija.bioshot.core.database.InjectionLogDao
import com.fkurevija.bioshot.core.database.MedicationDao
import com.fkurevija.bioshot.core.database.SymptomDao
import com.fkurevija.bioshot.core.database.SettingsRepository
import com.fkurevija.bioshot.core.database.SymptomEntryEntity
import com.fkurevija.bioshot.core.model.DeviceType
import com.fkurevija.bioshot.core.model.InjectionSite
import com.fkurevija.bioshot.core.model.ScheduleType
import com.fkurevija.bioshot.core.pk.PkCalculator
import com.fkurevija.bioshot.core.schedule.ScheduleEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BioShotViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = application

    /** Set when a database migration failed and the app fell back to an empty database. */
    val migrationError: MutableStateFlow<String?> = MutableStateFlow(null)

    private var database: BioShotDatabase? = null
    private val medicationDao: MedicationDao? by lazy { database?.medicationDao() }
    private val injectionLogDao: InjectionLogDao? by lazy { database?.injectionLogDao() }
    private val symptomDao: SymptomDao? by lazy { database?.symptomDao() }

    private val settingsRepository = SettingsRepository(appContext)

    init {
        try {
            database = BioShotDatabase.getDatabase(appContext)
        } catch (e: DatabaseMigrationException) {
            migrationError.value = e.message
            // Fall back to an empty database so the app still launches.
            BioShotDatabase.recreateDatabase(appContext)
            database = BioShotDatabase.getDatabase(appContext)
        }
    }

    /** Drops all data and recreates the database from scratch. Last-resort recovery. */
    fun resetDatabase() {
        database = null
        migrationError.value = null
        BioShotDatabase.recreateDatabase(appContext)
        database = BioShotDatabase.getDatabase(appContext)
    }

    /** Verifies database integrity. Preserves any migration error already detected at startup;
     * otherwise runs a lightweight query to validate the database is accessible. */
    fun checkDatabaseErrors() {
        viewModelScope.launch {
            // Preserve any migration error already detected at startup — the fallback
            // empty DB may be healthy, but the migration failure is still actionable.
            if (migrationError.value != null) return@launch
            try {
                database?.openHelper?.readableDatabase?.query("SELECT 1")?.use { cursor ->
                    cursor.moveToFirst()
                }
            } catch (e: DatabaseMigrationException) {
                migrationError.value = e.message
            } catch (e: Exception) {
                migrationError.value = e.message ?: "Database check failed with an unknown error"
            }
        }
    }

    val forceDark: StateFlow<Boolean> = settingsRepository.forceDark
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val remindersEnabled: StateFlow<Boolean> = settingsRepository.remindersEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setForceDark(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setForceDark(enabled) }
    }

    fun setRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setRemindersEnabled(enabled) }
    }

    val medications: StateFlow<List<MedicationEntity>> = medicationDao?.getAllMedications()
        ?.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        ?: MutableStateFlow(emptyList())

    val injectionLogs: StateFlow<List<InjectionLogEntity>> = injectionLogDao?.getAllInjectionLogs()
        ?.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        ?: MutableStateFlow(emptyList())

    val symptomEntries: StateFlow<List<SymptomEntryEntity>> = symptomDao?.getAllSymptomEntries()
        ?.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        ?: MutableStateFlow(emptyList())

    fun insertMedication(name: String, dosage: String, frequency: ScheduleType, customIntervalDays: Int, deviceType: DeviceType, notes: String?) {
        viewModelScope.launch {
            medicationDao?.insertMedication(
                MedicationEntity(
                    name = name,
                    dosage = dosage,
                    frequency = frequency,
                    customIntervalDays = customIntervalDays,
                    deviceType = deviceType,
                    notes = notes
                )
            )
        }
    }

    fun updateMedication(medication: MedicationEntity) {
        viewModelScope.launch {
            medicationDao?.updateMedication(medication)
        }
    }

    fun deleteMedication(medication: MedicationEntity) {
        viewModelScope.launch {
            medicationDao?.deleteInjectionLogsForMedication(medication.id)
            medicationDao?.deleteMedication(medication)
        }
    }

    fun insertInjectionLog(medicationId: Long, site: InjectionSite, dosageGiven: String, lotNumber: String?, notes: String?, painLevel: Int?, sideEffects: List<String>) {
        viewModelScope.launch {
            injectionLogDao?.insertInjectionLog(
                InjectionLogEntity(
                    medicationId = medicationId,
                    site = site,
                    dosageGiven = dosageGiven,
                    lotNumber = lotNumber,
                    notes = notes,
                    painLevel = painLevel?.coerceIn(0, 10),
                    sideEffects = sideEffects
                )
            )
        }
    }

    fun deleteInjectionLog(log: InjectionLogEntity) {
        viewModelScope.launch {
            injectionLogDao?.deleteInjectionLog(log)
        }
    }

    fun insertSymptomEntry(severity: Int, symptomName: String, notes: String?, tags: List<String>) {
        viewModelScope.launch {
            symptomDao?.insertSymptomEntry(
                SymptomEntryEntity(
                    severity = severity.coerceIn(1, 10),
                    symptomName = symptomName,
                    notes = notes,
                    tags = tags
                )
            )
        }
    }

    fun deleteSymptomEntry(entry: SymptomEntryEntity) {
        viewModelScope.launch {
            symptomDao?.deleteSymptomEntry(entry)
        }
    }
}
