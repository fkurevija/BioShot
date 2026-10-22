package com.fkurevija.bioshot.feature.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fkurevija.bioshot.core.database.BioShotDatabase
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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BioShotViewModel(application: Application) : AndroidViewModel(application) {
    private val database = BioShotDatabase.getDatabase(application)
    private val medicationDao: MedicationDao = database.medicationDao()
    private val injectionLogDao: InjectionLogDao = database.injectionLogDao()
    private val symptomDao: SymptomDao = database.symptomDao()

    private val settingsRepository = SettingsRepository(application)

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

    val medications: StateFlow<List<MedicationEntity>> = medicationDao.getAllMedications()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val injectionLogs: StateFlow<List<InjectionLogEntity>> = injectionLogDao.getAllInjectionLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val symptomEntries: StateFlow<List<SymptomEntryEntity>> = symptomDao.getAllSymptomEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun insertMedication(name: String, dosage: String, frequency: ScheduleType, deviceType: DeviceType, notes: String?) {
        viewModelScope.launch {
            medicationDao.insertMedication(
                MedicationEntity(
                    name = name,
                    dosage = dosage,
                    frequency = frequency,
                    deviceType = deviceType,
                    notes = notes
                )
            )
        }
    }

    fun updateMedication(medication: MedicationEntity) {
        viewModelScope.launch {
            medicationDao.updateMedication(medication)
        }
    }

    fun deleteMedication(medication: MedicationEntity) {
        viewModelScope.launch {
            medicationDao.deleteInjectionLogsForMedication(medication.id)
            medicationDao.deleteMedication(medication)
        }
    }

    fun insertInjectionLog(medicationId: Long, site: InjectionSite, dosageGiven: String, lotNumber: String?, notes: String?, painLevel: Int?, sideEffects: List<String>) {
        viewModelScope.launch {
            injectionLogDao.insertInjectionLog(
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
            injectionLogDao.deleteInjectionLog(log)
        }
    }

    fun insertSymptomEntry(severity: Int, symptomName: String, notes: String?, tags: List<String>) {
        viewModelScope.launch {
            symptomDao.insertSymptomEntry(
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
            symptomDao.deleteSymptomEntry(entry)
        }
    }
}
