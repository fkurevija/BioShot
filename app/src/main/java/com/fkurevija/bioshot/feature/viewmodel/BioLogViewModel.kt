package com.fkurevija.bioshot.feature.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fkurevija.bioshot.core.database.BioShotDatabase
import com.fkurevija.bioshot.core.database.DatabaseMigrationException
import com.fkurevija.bioshot.core.database.DefaultInjectionLogRepository
import com.fkurevija.bioshot.core.database.DefaultMedicationRepository
import com.fkurevija.bioshot.core.database.DefaultSymptomRepository
import com.fkurevija.bioshot.core.database.DefaultWeightRepository
import com.fkurevija.bioshot.core.database.InjectionLogEntity
import com.fkurevija.bioshot.core.database.InjectionLogRepository
import com.fkurevija.bioshot.core.database.MedicationEntity
import com.fkurevija.bioshot.core.database.MedicationRepository
import com.fkurevija.bioshot.core.database.SettingsRepository
import com.fkurevija.bioshot.core.database.SymptomEntryEntity
import com.fkurevija.bioshot.core.database.SymptomRepository
import com.fkurevija.bioshot.core.database.WeightLogEntity
import com.fkurevija.bioshot.core.database.WeightRepository
import com.fkurevija.bioshot.core.model.DeviceType
import com.fkurevija.bioshot.core.model.InjectionSite
import com.fkurevija.bioshot.core.model.ScheduleType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BioShotViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = application

    /** Set when a database migration failed and the app fell back to an empty database. */
    val migrationError: MutableStateFlow<String?> = MutableStateFlow(null)

    private var database: BioShotDatabase? = null
    private var medicationRepository: MedicationRepository? = null
    private var injectionLogRepository: InjectionLogRepository? = null
    private var symptomRepository: SymptomRepository? = null
    private var weightRepository: WeightRepository? = null

    val settingsRepository = SettingsRepository(appContext)

    init {
        initDatabaseAndRepositories()
    }

    private fun initDatabaseAndRepositories() {
        try {
            database = BioShotDatabase.getDatabase(appContext)
        } catch (e: DatabaseMigrationException) {
            migrationError.value = e.message
            BioShotDatabase.recreateDatabase(appContext)
            database = BioShotDatabase.getDatabase(appContext)
        }
        val db = database
        if (db != null) {
            medicationRepository = DefaultMedicationRepository(db.medicationDao())
            injectionLogRepository = DefaultInjectionLogRepository(db.injectionLogDao())
            symptomRepository = DefaultSymptomRepository(db.symptomDao())
            weightRepository = DefaultWeightRepository(db.weightDao())
        }
    }

    /** Drops all data and recreates the database from scratch. Last-resort recovery. */
    fun resetDatabase() {
        database = null
        migrationError.value = null
        BioShotDatabase.recreateDatabase(appContext)
        initDatabaseAndRepositories()
    }

    /** Verifies database integrity. */
    fun checkDatabaseErrors() {
        viewModelScope.launch {
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
    val reminderHour: StateFlow<Int> = settingsRepository.reminderHour
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), -1)
    val reminderMinute: StateFlow<Int> = settingsRepository.reminderMinute
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), -1)

    fun setForceDark(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setForceDark(enabled) }
    }

    fun setRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setRemindersEnabled(enabled) }
    }

    fun setReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch { settingsRepository.setReminderTime(hour, minute) }
    }

    val medications: StateFlow<List<MedicationEntity>> = medicationRepository?.getAllMedications()
        ?.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        ?: MutableStateFlow(emptyList())

    val injectionLogs: StateFlow<List<InjectionLogEntity>> = injectionLogRepository?.getAllInjectionLogs()
        ?.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        ?: MutableStateFlow(emptyList())

    val symptomEntries: StateFlow<List<SymptomEntryEntity>> = symptomRepository?.getAllSymptomEntries()
        ?.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        ?: MutableStateFlow(emptyList())

    val weightLogs: StateFlow<List<WeightLogEntity>> = weightRepository?.getAllWeightLogs()
        ?.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        ?: MutableStateFlow(emptyList())

    fun insertMedication(name: String, dosage: String, frequency: ScheduleType, customIntervalDays: Int, deviceType: DeviceType, notes: String?) {
        viewModelScope.launch {
            medicationRepository?.insertMedication(
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
            medicationRepository?.updateMedication(medication)
        }
    }

    fun deleteMedication(medication: MedicationEntity) {
        viewModelScope.launch {
            medicationRepository?.deleteMedication(medication)
        }
    }

    fun insertInjectionLog(medicationId: Long, site: InjectionSite, dosageGiven: String, lotNumber: String?, notes: String?, painLevel: Int?, sideEffects: List<String>, timestamp: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            injectionLogRepository?.insertInjectionLog(
                InjectionLogEntity(
                    medicationId = medicationId,
                    timestamp = timestamp,
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
            injectionLogRepository?.deleteInjectionLog(log)
        }
    }

    fun updateInjectionLog(log: InjectionLogEntity) {
        viewModelScope.launch {
            injectionLogRepository?.updateInjectionLog(log)
        }
    }

    fun insertSymptomEntry(severity: Int, symptomName: String, notes: String?, tags: List<String>) {
        viewModelScope.launch {
            symptomRepository?.insertSymptomEntry(
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
            symptomRepository?.deleteSymptomEntry(entry)
        }
    }

    fun insertWeightLog(weight: Double, unit: String, bodyFatPercentage: Double?, waistCm: Double?, notes: String?, timestamp: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            weightRepository?.insertWeightLog(
                WeightLogEntity(
                    timestamp = timestamp,
                    weight = weight,
                    unit = unit,
                    bodyFatPercentage = bodyFatPercentage,
                    waistCm = waistCm,
                    notes = notes
                )
            )
        }
    }

    fun deleteWeightLog(log: WeightLogEntity) {
        viewModelScope.launch {
            weightRepository?.deleteWeightLog(log)
        }
    }
}
