package com.fkurevija.bioshot.core.database

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.room.Room
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.collect
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import com.fkurevija.bioshot.core.model.DeviceType
import com.fkurevija.bioshot.core.model.InjectionSite
import com.fkurevija.bioshot.core.model.ScheduleType

/**
 * Instrumented DAO tests for the data layer. These run on an Android device/emulator (with a real
 * Application context) so that Room databases are created and used as they would be in production.
 * They cover the medication cascade delete and the typed list (JSON) persistence.
 */
@RunWith(AndroidJUnit4::class)
class MedicationDaoTest {
    private lateinit var db: BioShotDatabase
    private lateinit var medicationDao: MedicationDao
    private lateinit var injectionLogDao: InjectionLogDao

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, BioShotDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        medicationDao = db.medicationDao()
        injectionLogDao = db.injectionLogDao()
    }

    @After
    fun close() {
        db.close()
    }

    @Test
    fun deletingMedicationAlsoRemovesAssociatedInjectionLogs() = runBlocking {
        val newMedication = MedicationEntity(
            name = "Humira",
            dosage = "40mg",
            frequency = ScheduleType.WEEKLY,
            deviceType = DeviceType.PEN
        )
        val medId = medicationDao.insertMedication(newMedication)

        injectionLogDao.insertInjectionLog(
            InjectionLogEntity(
                medicationId = medId,
                site = InjectionSite.ABDOMEN_LEFT,
                dosageGiven = "40mg",
                sideEffects = listOf("nausea", "headache")
            )
        )
        injectionLogDao.insertInjectionLog(
            InjectionLogEntity(
                medicationId = medId,
                site = InjectionSite.THIGH_LEFT,
                dosageGiven = "40mg",
                sideEffects = emptyList()
            )
        )

        val logsBefore = mutableListOf<InjectionLogEntity>()
        injectionLogDao.getAllInjectionLogs().collect { logsBefore.addAll(it) }
        assertEquals(2, logsBefore.size)

        medicationDao.deleteMedication(
            MedicationEntity(
                id = medId,
                name = "Humira",
                dosage = "40mg",
                frequency = ScheduleType.WEEKLY,
                deviceType = DeviceType.PEN
            )
        )

        val logsAfter = mutableListOf<InjectionLogEntity>()
        injectionLogDao.getAllInjectionLogs().collect { logsAfter.addAll(it) }
        assertTrue(logsAfter.isEmpty())
    }

    @Test
    fun medicationAndLogsPersistWithCorrectTypes() = runBlocking {
        val newMedication = MedicationEntity(
            name = "Biosimilar",
            dosage = "20mg",
            frequency = ScheduleType.BIWEEKLY,
            deviceType = DeviceType.SYRINGE,
            notes = "Take with food"
        )
        val medId = medicationDao.insertMedication(newMedication)

        val logId = injectionLogDao.insertInjectionLog(
            InjectionLogEntity(
                medicationId = medId,
                site = InjectionSite.ARM_LEFT,
                dosageGiven = "20mg",
                lotNumber = "LOT-123",
                notes = "First dose",
                painLevel = 3,
                sideEffects = listOf("rash")
            )
        )

        val loadedMedication = medicationDao.getMedicationById(medId)
        assertNotNull(loadedMedication)
        assertEquals("Biosimilar", loadedMedication?.name)
        assertEquals("20mg", loadedMedication?.dosage)
        assertEquals(ScheduleType.BIWEEKLY, loadedMedication?.frequency)

        val logs = mutableListOf<InjectionLogEntity>()
        injectionLogDao.getLogsForMedication(medId).collect { logs.addAll(it) }
        assertEquals(1, logs.size)
        val log = logs.first()
        assertEquals("First dose", log.notes)
        assertEquals("LOT-123", log.lotNumber)
        assertEquals(3, log.painLevel)
        assertEquals(listOf("rash"), log.sideEffects)
    }
}
