package com.bartman79.elisa.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserDataManager(private val context: Context) {

    suspend fun isBirthdayToday(): Boolean {
        val birthdayStr = userBirthday.first()
        if (birthdayStr.isBlank()) return false
        return try {
            val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val birthDate = formatter.parse(birthdayStr)
            val calendarBirth = Calendar.getInstance().apply { time = birthDate }
            val calendarToday = Calendar.getInstance()
            calendarBirth.get(Calendar.DAY_OF_MONTH) == calendarToday.get(Calendar.DAY_OF_MONTH) &&
                    calendarBirth.get(Calendar.MONTH) == calendarToday.get(Calendar.MONTH)
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        private val USER_NAME = stringPreferencesKey("user_name")
        private val USER_BIRTHDAY = stringPreferencesKey("user_birthday")
    }

    val userName: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[USER_NAME] ?: "" }

    val userBirthday: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[USER_BIRTHDAY] ?: "" }

    suspend fun saveUserData(name: String, birthday: String) {
        context.dataStore.edit { prefs ->
            if (name.isNotBlank()) prefs[USER_NAME] = name
            if (birthday.isNotBlank()) prefs[USER_BIRTHDAY] = birthday
        }
    }
}