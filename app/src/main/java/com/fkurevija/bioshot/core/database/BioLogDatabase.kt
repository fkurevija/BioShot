package com.fkurevija.bioshot.core.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.serialization.json.Json
import java.io.File

@Database(
    entities = [
        MedicationEntity::class,
        InjectionLogEntity::class,
        SymptomEntryEntity::class
    ],
    version = 2,
    exportSchema = true
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
                val instance = buildDatabase(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }

        private fun buildDatabase(context: Context): BioShotDatabase {
            // Pre-migration backup: if an older database exists, export all data before
            // Room runs migrations. Backup failures are non-fatal; migration proceeds regardless.
            val currentVersion = getExistingDatabaseVersion(context)
            if (currentVersion > 0 && currentVersion < 2) {
                try {
                    DatabaseBackupManager.backupBeforeMigration(context, currentVersion)
                } catch (_: Exception) {
                    // Non-fatal: we tried.
                }
            }

            val builder = Room.databaseBuilder(
                context,
                BioShotDatabase::class.java,
                "bioshot_database"
            )
                .addMigrations(migration1To2)

            return try {
                builder.build()
            } catch (e: Exception) {
                throw DatabaseMigrationException(
                    "Room database migration failed. Your data backup is in the app's " +
                            "files directory under bioshot_backups/.",
                    e
                )
            }
        }

        private fun getExistingDatabaseVersion(context: Context): Int {
            val dbFile = File(context.filesDir, "bioshot_database")
            if (!dbFile.exists()) return 0
            val db = SQLiteDatabase.openDatabase(dbFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
            return try {
                val cursor = db.rawQuery("PRAGMA user_version", null)
                try {
                    if (cursor.moveToFirst()) cursor.getInt(0) else 0
                } finally {
                    cursor.close()
                }
            } finally {
                db.close()
            }
        }

        /** Drops the database file and clears the singleton so it can be recreated empty. */
        fun recreateDatabase(context: Context) {
            INSTANCE = null
            File(context.filesDir, "bioshot_database").delete()
            File(context.filesDir, "bioshot_database-wal").delete()
            File(context.filesDir, "bioshot_database-shm").delete()
        }

        private val migration1To2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Introduce the new typed list columns first, then backfill from the legacy
                // comma-separated CSV columns. The legacy columns are left in place (harmless
                // extra columns) because DROP COLUMN is not supported on older SQLite versions.
                database.execSQL("ALTER TABLE injection_logs ADD COLUMN sideEffects TEXT NOT NULL DEFAULT '[]'")
                database.execSQL("ALTER TABLE symptom_entries ADD COLUMN tags TEXT NOT NULL DEFAULT '[]'")

                convertLegacyColumn(database, "injection_logs", "sideEffectsJson", "sideEffects")
                convertLegacyColumn(database, "symptom_entries", "tagsJson", "tags")
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
