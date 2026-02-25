package com.example.core.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

val Context.authPrefsDataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")
