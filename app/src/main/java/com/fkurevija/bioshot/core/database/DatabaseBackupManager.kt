package com.fkurevija.bioshot.core.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.io.File

/**
 * Creates a JSON backup of all Room tables before a schema migration runs.
 *
 * This is a safety net: if a migration is incompatible and crashes the app on open,
 * the user has a recoverable backup file in the app's files directory.
 */
object DatabaseBackupManager {

    private const val DB_NAME = "bioshot_database"
    private const val BACKUP_DIR = "bioshot_backups"

    /** Directory where backup JSON files are stored. */
    fun getBackupDir(context: Context): File {
        return context.getExternalFilesDir(null)?.let { File(it, BACKUP_DIR) }
            ?: File(context.filesDir, BACKUP_DIR).apply { mkdirs() }
    }

    /** Path for a backup file tagged with the schema version being migrated from. */
    fun getBackupFile(context: Context, currentVersion: Int): File {
        val dir = getBackupDir(context)
        dir.mkdirs()
        return File(dir, "bioshot_backup_v${currentVersion}_${System.currentTimeMillis()}.json")
    }

    /**
     * Exports every row from all Room tables into a manual JSON backup file.
     */
    fun createManualBackup(context: Context): File? {
        val dbFile = File(context.filesDir, DB_NAME)
        if (!dbFile.exists()) return null

        val db = SQLiteDatabase.openDatabase(dbFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
        return try {
            val backup = buildJsonObject {
                put("schemaVersion", JsonPrimitive(4))
                put("backupTimestamp", JsonPrimitive(System.currentTimeMillis()))
                put("tables", buildJsonObject {
                    put("medications", exportTable(db, "medications"))
                    put("injection_logs", exportTable(db, "injection_logs"))
                    put("symptom_entries", exportTable(db, "symptom_entries"))
                    put("weight_logs", exportTable(db, "weight_logs"))
                })
            }

            val dir = getBackupDir(context)
            val file = File(dir, "bioshot_manual_backup_${System.currentTimeMillis()}.json")
            file.writeText(Json.encodeToString(backup))
            file
        } finally {
            db.close()
        }
    }

    /**
     * Exports every row from all three Room tables into a single JSON file.
     * Returns the backup file, or null if no existing database exists.
     */
    fun backupBeforeMigration(context: Context, currentVersion: Int): File? {
        val dbFile = File(context.filesDir, DB_NAME)
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