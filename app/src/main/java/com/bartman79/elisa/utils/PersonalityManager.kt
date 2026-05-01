package com.bartman79.elisa.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "personality_prefs")

enum class PersonalityType(val value: Int) {
    FRIEND(1),
    SISTER(2),
    COACH(3);

    companion object {
        fun fromInt(value: Int): PersonalityType {
            return values().firstOrNull { it.value == value } ?: FRIEND
        }
    }
}

class PersonalityManager(private val context: Context) {

    companion object {
        private val PERSONALITY_KEY = intPreferencesKey("personality")
    }

    val currentPersonality: Flow<PersonalityType> = context.dataStore.data
        .map { prefs ->
            PersonalityType.fromInt(prefs[PERSONALITY_KEY] ?: 1)
        }

    suspend fun setPersonality(type: PersonalityType) {
        context.dataStore.edit { prefs ->
            prefs[PERSONALITY_KEY] = type.value
        }
    }
}