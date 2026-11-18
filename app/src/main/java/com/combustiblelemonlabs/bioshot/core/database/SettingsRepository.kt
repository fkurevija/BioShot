package com.combustiblelemonlabs.bioshot.core.database

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.combustiblelemonlabs.bioshot.core.notifications.ReminderInterval
import com.combustiblelemonlabs.bioshot.core.notifications.ReminderMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.bioshotDataStore: DataStore<Preferences> by preferencesDataStore(name = "bioshot_settings")

/**
 * Repository over DataStore preferences for app settings (dark theme, reminder preferences, notification time).
 */
class SettingsRepository(private val context: Context) {

    private val dataStore = context.bioshotDataStore

    private val forceDarkKey = booleanPreferencesKey("forceDark")
    private val remindersEnabledKey = booleanPreferencesKey("remindersEnabled")
    private val reminderModeKey = stringPreferencesKey("reminderMode")
    private val reminderIntervalsKey = stringSetPreferencesKey("reminderIntervals")
    private val reminderHourKey = intPreferencesKey("reminderHour")
    private val reminderMinuteKey = intPreferencesKey("reminderMinute")

    val forceDark: Flow<Boolean> = dataStore.data.map { prefs: Preferences ->
        prefs[forceDarkKey] ?: false
    }

    val remindersEnabled: Flow<Boolean> = dataStore.data.map { prefs: Preferences ->
        val legacyEnabled = prefs[remindersEnabledKey] ?: false
        val modeStr = prefs[reminderModeKey]
        if (modeStr != null) {
            modeStr != ReminderMode.OFF.name
        } else {
            legacyEnabled
        }
    }

    val reminderMode: Flow<ReminderMode> = dataStore.data.map { prefs: Preferences ->
        val modeStr = prefs[reminderModeKey]
        if (modeStr != null) {
            try {
                ReminderMode.valueOf(modeStr)
            } catch (e: Exception) {
                ReminderMode.OFF
            }
        } else {
            val legacyEnabled = prefs[remindersEnabledKey] ?: false
            if (legacyEnabled) ReminderMode.SINGLE else ReminderMode.OFF
        }
    }

    val reminderIntervals: Flow<Set<ReminderInterval>> = dataStore.data.map { prefs: Preferences ->
        val setStrings = prefs[reminderIntervalsKey]
        if (setStrings != null) {
            setStrings.mapNotNull { ReminderInterval.fromName(it) }.toSet()
        } else {
            setOf(ReminderInterval.ONE_DAY_BEFORE)
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
            prefs[reminderModeKey] = if (enabled) ReminderMode.SINGLE.name else ReminderMode.OFF.name
        }
    }

    suspend fun setReminderMode(mode: ReminderMode) {
        dataStore.edit { prefs ->
            prefs[reminderModeKey] = mode.name
            prefs[remindersEnabledKey] = (mode != ReminderMode.OFF)
        }
    }

    suspend fun setReminderIntervals(intervals: Set<ReminderInterval>) {
        dataStore.edit { prefs ->
            prefs[reminderIntervalsKey] = intervals.map { it.name }.toSet()
        }
    }

    suspend fun setReminderTime(hour: Int, minute: Int) {
        dataStore.edit { prefs ->
            prefs[reminderHourKey] = hour
            prefs[reminderMinuteKey] = minute
        }
    }
}
