package com.fkurevija.bioshot.core.database

import kotlinx.coroutines.flow.Flow

interface InjectionLogRepository {
    fun getAllInjectionLogs(): Flow<List<InjectionLogEntity>>
    fun getLogsForMedication(medicationId: Long): Flow<List<InjectionLogEntity>>
    suspend fun getInjectionLogById(logId: Long): InjectionLogEntity?
    suspend fun insertInjectionLog(log: InjectionLogEntity): Long
    suspend fun updateInjectionLog(log: InjectionLogEntity)
    suspend fun deleteInjectionLog(log: InjectionLogEntity)
}

class DefaultInjectionLogRepository(
    private val injectionLogDao: InjectionLogDao
) : InjectionLogRepository {
    override fun getAllInjectionLogs(): Flow<List<InjectionLogEntity>> = injectionLogDao.getAllInjectionLogs()
    override fun getLogsForMedication(medicationId: Long): Flow<List<InjectionLogEntity>> = injectionLogDao.getLogsForMedication(medicationId)
    override suspend fun getInjectionLogById(logId: Long): InjectionLogEntity? = injectionLogDao.getInjectionLogById(logId)
    override suspend fun insertInjectionLog(log: InjectionLogEntity): Long = injectionLogDao.insertInjectionLog(log)
    override suspend fun updateInjectionLog(log: InjectionLogEntity) = injectionLogDao.updateInjectionLog(log)
    override suspend fun deleteInjectionLog(log: InjectionLogEntity) = injectionLogDao.deleteInjectionLog(log)
}
