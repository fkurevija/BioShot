package com.combustiblelemonlabs.bioshot.core.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File

/**
 * Creates a JSON backup of all Room tables before a schema migration runs,
 * or exports a manual JSON backup using Room DAOs safely.
 */
object DatabaseBackupManager {

    private const val DB_NAME = "bioshot_database"
    private const val BACKUP_DIR = "bioshot_backups"

    /** Directory where backup JSON files are stored. */
    fun getBackupDir(context: Context): File {
        val dir = context.getExternalFilesDir(null)?.let { File(it, BACKUP_DIR) }
            ?: File(context.filesDir, BACKUP_DIR)
        dir.mkdirs()
        return dir
    }

    /** Path for a backup file tagged with the schema version being migrated from. */
    fun getBackupFile(context: Context, currentVersion: Int): File {
        val dir = getBackupDir(context)
        dir.mkdirs()
        return File(dir, "bioshot_backup_v${currentVersion}_${System.currentTimeMillis()}.json")
    }

    /**
     * Exports every row from all Room tables into a manual JSON backup file safely via Room DAOs.
     */
    suspend fun createManualBackup(context: Context): File? {
        return try {
            val db = BioShotDatabase.getDatabase(context)
            val meds = db.medicationDao().getAllMedicationsList()
            val logs = db.injectionLogDao().getAllInjectionLogsList()
            val symptoms = db.symptomDao().getAllSymptomEntriesList()
            val weights = db.weightDao().getAllWeightLogsList()

            val backup = buildJsonObject {
                put("schemaVersion", JsonPrimitive(4))
                put("backupTimestamp", JsonPrimitive(System.currentTimeMillis()))
                put("tables", buildJsonObject {
                    put("medications", Json.encodeToJsonElement(meds))
                    put("injection_logs", Json.encodeToJsonElement(logs))
                    put("symptom_entries", Json.encodeToJsonElement(symptoms))
                    put("weight_logs", Json.encodeToJsonElement(weights))
                })
            }

            val dir = getBackupDir(context)
            val file = File(dir, "bioshot_manual_backup_${System.currentTimeMillis()}.json")
            file.writeText(Json.encodeToString(backup))
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Exports every row from all three Room tables into a single JSON file.
     * Returns the backup file, or null if no existing database exists.
     */
    fun backupBeforeMigration(context: Context, currentVersion: Int): File? {
        val dbFile = context.getDatabasePath(DB_NAME)
        if (!dbFile.exists()) return null

        val db = SQLiteDatabase.openDatabase(dbFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
        return try {
            val backup = buildJsonObject {
                put("schemaVersion", JsonPrimitive(currentVersion))
                put("backupTimestamp", JsonPrimitive(System.currentTimeMillis()))
                put("tables", buildJsonObject {
                    put("medications", exportTable(db, "medications"))
                    put("injection_logs", exportTable(db, "injection_logs"))
                    put("symptom_entries", exportTable(db, "symptom_entries"))
                })
            }

            val file = getBackupFile(context, currentVersion)
            file.writeText(Json.encodeToString(backup))
            file
        } finally {
            db.close()
        }
    }

    private fun exportTable(db: SQLiteDatabase, table: String): kotlinx.serialization.json.JsonArray {
        return buildJsonArray {
            val c = db.query(table, null, null, null, null, null, null)
            try {
                while (c.moveToNext()) {
                    add(buildJsonObject {
                        for (i in 0 until c.columnCount) {
                        if (!c.isNull(i)) {
                            put(c.getColumnName(i), JsonPrimitive(c.getString(i)))
                        }
                        }
                    })
                }
            } finally {
                c.close()
            }
        }
    }
}
