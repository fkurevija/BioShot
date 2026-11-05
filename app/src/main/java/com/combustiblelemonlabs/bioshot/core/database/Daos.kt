package com.combustiblelemonlabs.bioshot.core.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medications ORDER BY createdAt DESC")
    fun getAllMedications(): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications ORDER BY createdAt DESC")
    suspend fun getAllMedicationsList(): List<MedicationEntity>

    @Query("SELECT * FROM medications WHERE id = :medicationId")
    suspend fun getMedicationById(medicationId: Long): MedicationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: MedicationEntity): Long

    @Update
    suspend fun updateMedication(medication: MedicationEntity)

    @Query("DELETE FROM injection_logs WHERE medicationId = :medicationId")
    suspend fun deleteInjectionLogsForMedication(medicationId: Long)

    @Delete
    suspend fun deleteMedication(medication: MedicationEntity)
}

@Dao
interface InjectionLogDao {
    @Query("SELECT * FROM injection_logs ORDER BY timestamp DESC")
    fun getAllInjectionLogs(): Flow<List<InjectionLogEntity>>

    @Query("SELECT * FROM injection_logs ORDER BY timestamp DESC")
    suspend fun getAllInjectionLogsList(): List<InjectionLogEntity>

    @Query("SELECT * FROM injection_logs WHERE medicationId = :medicationId ORDER BY timestamp DESC")
    fun getLogsForMedication(medicationId: Long): Flow<List<InjectionLogEntity>>

    @Query("SELECT * FROM injection_logs WHERE id = :logId")
    suspend fun getInjectionLogById(logId: Long): InjectionLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInjectionLog(log: InjectionLogEntity): Long

    @Update
    suspend fun updateInjectionLog(log: InjectionLogEntity)

    @Delete
    suspend fun deleteInjectionLog(log: InjectionLogEntity)
}

@Dao
interface SymptomDao {
    @Query("SELECT * FROM symptom_entries ORDER BY timestamp DESC")
    fun getAllSymptomEntries(): Flow<List<SymptomEntryEntity>>

    @Query("SELECT * FROM symptom_entries ORDER BY timestamp DESC")
    suspend fun getAllSymptomEntriesList(): List<SymptomEntryEntity>

    @Query("SELECT * FROM symptom_entries WHERE id = :entryId")
    suspend fun getSymptomEntryById(entryId: Long): SymptomEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSymptomEntry(entry: SymptomEntryEntity): Long

    @Update
    suspend fun updateSymptomEntry(entry: SymptomEntryEntity)

    @Delete
    suspend fun deleteSymptomEntry(entry: SymptomEntryEntity)
}

@Dao
interface WeightDao {
    @Query("SELECT * FROM weight_logs ORDER BY timestamp DESC")
    fun getAllWeightLogs(): Flow<List<WeightLogEntity>>

    @Query("SELECT * FROM weight_logs ORDER BY timestamp DESC")
    suspend fun getAllWeightLogsList(): List<WeightLogEntity>

    @Query("SELECT * FROM weight_logs WHERE id = :logId")
    suspend fun getWeightLogById(logId: Long): WeightLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeightLog(log: WeightLogEntity): Long

    @Update
    suspend fun updateWeightLog(log: WeightLogEntity)

    @Delete
    suspend fun deleteWeightLog(log: WeightLogEntity)
}
