package com.combustiblelemonlabs.bioshot.core.database

import kotlinx.coroutines.flow.Flow

interface MedicationRepository {
    fun getAllMedications(): Flow<List<MedicationEntity>>
    suspend fun getMedicationById(medicationId: Long): MedicationEntity?
    suspend fun insertMedication(medication: MedicationEntity): Long
    suspend fun updateMedication(medication: MedicationEntity)
    suspend fun deleteMedication(medication: MedicationEntity)
}

class DefaultMedicationRepository(
    private val medicationDao: MedicationDao
) : MedicationRepository {
    override fun getAllMedications(): Flow<List<MedicationEntity>> = medicationDao.getAllMedications()
    override suspend fun getMedicationById(medicationId: Long): MedicationEntity? = medicationDao.getMedicationById(medicationId)
    override suspend fun insertMedication(medication: MedicationEntity): Long = medicationDao.insertMedication(medication)
    override suspend fun updateMedication(medication: MedicationEntity) = medicationDao.updateMedication(medication)
    override suspend fun deleteMedication(medication: MedicationEntity) {
        medicationDao.deleteInjectionLogsForMedication(medication.id)
        medicationDao.deleteMedication(medication)
    }
}
