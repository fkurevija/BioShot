package com.fkurevija.bioshot.core.database

/** Thrown when a Room database migration fails and existing data cannot be preserved automatically. */
class DatabaseMigrationException(message: String, cause: Throwable? = null) : Exception(message, cause)