package com.fkurevija.bioshot.core.database

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.bioshotDataStore: DataStore<Preferences> by preferencesDataStore(name = "bioshot_settings")

/**
 * Repository over DataStore preferences. Settings are persistent across app restarts so the user's
 * choices (dark theme, reminders) survive app restarts.
 */
class SettingsRepository(private val context: Context) {

    private val dataStore = context.bioshotDataStore

    val forceDark: Flow<Boolean> = dataStore.data.map { prefs: Preferences ->
        prefs[booleanPreferencesKey("forceDark")] ?: false
    }

    val remindersEnabled: Flow<Boolean> = dataStore.data.map { prefs: Preferences ->
        prefs[booleanPreferencesKey("remindersEnabled")] ?: false
    }

    suspend fun setForceDark(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[booleanPreferencesKey("forceDark")] = enabled
        }
    }

    suspend fun setRemindersEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[booleanPreferencesKey("remindersEnabled")] = enabled
        }
    }
}