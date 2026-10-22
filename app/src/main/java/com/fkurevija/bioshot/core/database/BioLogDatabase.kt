package com.fkurevija.bioshot.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.serialization.json.Json

@Database(
    entities = [
        MedicationEntity::class,
        InjectionLogEntity::class,
        SymptomEntryEntity::class
    ],
    version = 2,
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
                    .addMigrations(migration1To2)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private val migration1To2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Introduce the new typed list columns first, then backfill from the legacy
                // comma-separated CSV columns, then drop the legacy columns.
                database.execSQL("ALTER TABLE injection_logs ADD COLUMN side_effects TEXT")
                database.execSQL("ALTER TABLE symptom_entries ADD COLUMN tags TEXT")

                convertLegacyColumn(database, "injection_logs", "side_effects_json", "side_effects")
                convertLegacyColumn(database, "symptom_entries", "tags_json", "tags")

                database.execSQL("ALTER TABLE injection_logs DROP COLUMN side_effects_json")
                database.execSQL("ALTER TABLE symptom_entries DROP COLUMN tags_json")
            }
        }

        private fun convertLegacyColumn(
            database: SupportSQLiteDatabase,
            table: String,
            legacyColumn: String,
            targetColumn: String
        ) {
            val cursor = database.query("SELECT id, $legacyColumn FROM $table WHERE $legacyColumn IS NOT NULL")
            try {
                val idIndex = cursor.getColumnIndexOrThrow("id")
                val csvIndex = cursor.getColumnIndexOrThrow(legacyColumn)
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idIndex)
                    var legacyCsv = cursor.getString(csvIndex)
                    val list = if (legacyCsv.isNullOrBlank()) {
                        emptyList()
                    } else {
                        // Best-effort: if an old row happened to already contain valid JSON, decode it;
                        // otherwise treat it as the old comma-separated format.
                        try {
                            Json.decodeFromString<List<String>>(legacyCsv)
                        } catch (cause: Exception) {
                            legacyCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        }
                    }
                    val json = Json.encodeToString(list)
                    database.execSQL("UPDATE $table SET $targetColumn = ? WHERE id = ?", arrayOf(json, id))
                }
            } finally {
                cursor.close()
            }
        }
    }
}
