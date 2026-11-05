package com.combustiblelemonlabs.bioshot.core.database

import kotlinx.coroutines.flow.Flow

interface SymptomRepository {
    fun getAllSymptomEntries(): Flow<List<SymptomEntryEntity>>
    suspend fun getSymptomEntryById(entryId: Long): SymptomEntryEntity?
    suspend fun insertSymptomEntry(entry: SymptomEntryEntity): Long
    suspend fun updateSymptomEntry(entry: SymptomEntryEntity)
    suspend fun deleteSymptomEntry(entry: SymptomEntryEntity)
}

class DefaultSymptomRepository(
    private val symptomDao: SymptomDao
) : SymptomRepository {
    override fun getAllSymptomEntries(): Flow<List<SymptomEntryEntity>> = symptomDao.getAllSymptomEntries()
    override suspend fun getSymptomEntryById(entryId: Long): SymptomEntryEntity? = symptomDao.getSymptomEntryById(entryId)
    override suspend fun insertSymptomEntry(entry: SymptomEntryEntity): Long = symptomDao.insertSymptomEntry(entry)
    override suspend fun updateSymptomEntry(entry: SymptomEntryEntity) = symptomDao.updateSymptomEntry(entry)
    override suspend fun deleteSymptomEntry(entry: SymptomEntryEntity) = symptomDao.deleteSymptomEntry(entry)
}
