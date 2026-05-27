package com.striklewin.apps.core.persistence

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "dribble_master_prefs")

class GamePreferencesRepository(private val context: Context) {

    private val highScoreKey = intPreferencesKey("high_score")

    val highScoreFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[highScoreKey] ?: 0
    }

    suspend fun saveHighScore(value: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[highScoreKey] ?: 0
            if (value > current) {
                prefs[highScoreKey] = value
            }
        }
    }
}
