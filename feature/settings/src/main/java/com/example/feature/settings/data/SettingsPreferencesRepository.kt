package com.example.feature.settings.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.example.core.storage.appSettingsDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class SettingsPreferences(
    val pushNotificationsEnabled: Boolean = true,
    val dueDateRemindersEnabled: Boolean = true
)

class SettingsPreferencesRepository(
    private val context: Context
) {
    private companion object {
        val PUSH_NOTIFICATIONS_KEY = booleanPreferencesKey("push_notifications_enabled")
        val DUE_DATE_REMINDERS_KEY = booleanPreferencesKey("due_date_reminders_enabled")
    }

    fun observePreferences(): Flow<SettingsPreferences> {
        return context.appSettingsDataStore.data.map { preferences ->
            SettingsPreferences(
                pushNotificationsEnabled = preferences[PUSH_NOTIFICATIONS_KEY] ?: true,
                dueDateRemindersEnabled = preferences[DUE_DATE_REMINDERS_KEY] ?: true
            )
        }
    }

    suspend fun setPushNotificationsEnabled(enabled: Boolean) {
        context.appSettingsDataStore.edit { preferences ->
            preferences[PUSH_NOTIFICATIONS_KEY] = enabled
        }
    }

    suspend fun setDueDateRemindersEnabled(enabled: Boolean) {
        context.appSettingsDataStore.edit { preferences ->
            preferences[DUE_DATE_REMINDERS_KEY] = enabled
        }
    }
}
