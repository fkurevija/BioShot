package com.combustiblelemonlabs.bioshot.core.database

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.combustiblelemonlabs.bioshot.core.notifications.CustomReminder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.bioshotDataStore: DataStore<Preferences> by preferencesDataStore(name = "bioshot_settings")

/**
 * Repository over DataStore preferences for app settings (dark theme, reminder preferences, notification time).
 */
class SettingsRepository(private val context: Context) {

    private val dataStore = context.bioshotDataStore

    private val forceDarkKey = booleanPreferencesKey("forceDark")
    private val remindersEnabledKey = booleanPreferencesKey("remindersEnabled")
    private val customRemindersListKey = stringPreferencesKey("customRemindersList")
    private val reminderHourKey = intPreferencesKey("reminderHour")
    private val reminderMinuteKey = intPreferencesKey("reminderMinute")

    val forceDark: Flow<Boolean> = dataStore.data.map { prefs: Preferences ->
        prefs[forceDarkKey] ?: false
    }

    val remindersEnabled: Flow<Boolean> = dataStore.data.map { prefs: Preferences ->
        prefs[remindersEnabledKey] ?: false
    }

    val customReminders: Flow<List<CustomReminder>> = dataStore.data.map { prefs: Preferences ->
        val jsonStr = prefs[customRemindersListKey]
        if (!jsonStr.isNullOrBlank()) {
            try {
                Json.decodeFromString<List<CustomReminder>>(jsonStr)
            } catch (e: Exception) {
                CustomReminder.DEFAULT_REMINDERS
            }
        } else {
            CustomReminder.DEFAULT_REMINDERS
        }
    }

    val reminderHour: Flow<Int> = dataStore.data.map { prefs: Preferences ->
        prefs[reminderHourKey] ?: -1
    }

    val reminderMinute: Flow<Int> = dataStore.data.map { prefs: Preferences ->
        prefs[reminderMinuteKey] ?: -1
    }

    suspend fun setForceDark(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[forceDarkKey] = enabled
        }
    }

    suspend fun setRemindersEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[remindersEnabledKey] = enabled
        }
    }

    suspend fun setCustomReminders(reminders: List<CustomReminder>) {
        dataStore.edit { prefs ->
            val jsonStr = Json.encodeToString(reminders)
            prefs[customRemindersListKey] = jsonStr
        }
    }

    suspend fun setReminderTime(hour: Int, minute: Int) {
        dataStore.edit { prefs ->
            prefs[reminderHourKey] = hour
            prefs[reminderMinuteKey] = minute
        }
    }
}
