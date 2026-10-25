package com.fkurevija.bioshot.core.database

import kotlinx.coroutines.flow.Flow

interface WeightRepository {
    fun getAllWeightLogs(): Flow<List<WeightLogEntity>>
    suspend fun getWeightLogById(logId: Long): WeightLogEntity?
    suspend fun insertWeightLog(log: WeightLogEntity): Long
    suspend fun updateWeightLog(log: WeightLogEntity)
    suspend fun deleteWeightLog(log: WeightLogEntity)
}

class DefaultWeightRepository(
    private val weightDao: WeightDao
) : WeightRepository {
    override fun getAllWeightLogs(): Flow<List<WeightLogEntity>> = weightDao.getAllWeightLogs()
    override suspend fun getWeightLogById(logId: Long): WeightLogEntity? = weightDao.getWeightLogById(logId)
    override suspend fun insertWeightLog(log: WeightLogEntity): Long = weightDao.insertWeightLog(log)
    override suspend fun updateWeightLog(log: WeightLogEntity) = weightDao.updateWeightLog(log)
    override suspend fun deleteWeightLog(log: WeightLogEntity) = weightDao.deleteWeightLog(log)
}
