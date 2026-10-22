package com.fkurevija.bioshot.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        MedicationEntity::class,
        InjectionLogEntity::class,
        SymptomEntryEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BioShotDatabase : RoomDatabase() {

    abstract fun medicationDao(): MedicationDao
    abstract fun injectionLogDao(): InjectionLogDao
    abstract fun symptomDao(): SymptomDao

    companion object {
        @Volatile
        private var INSTANCE: BioShotDatabase? = null

        fun getDatabase(context: Context): BioShotDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BioShotDatabase::class.java,
                    "bioshot_database"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
