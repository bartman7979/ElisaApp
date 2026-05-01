package com.bartman79.elisa.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.bartman79.elisa.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "image_prefs")

class ImageManager(private val context: Context) {

    companion object {
        private val LAST_IMAGE_INDEX = intPreferencesKey("last_image_index")
    }
    // Список ID наших картинок из ресурсов
    private val imageList = listOf(
        R.drawable.misstress_5,
        R.drawable.misstress_3,
        R.drawable.misstress_2,
        R.drawable.misstress_1,
        R.drawable.misstress_7,
        R.drawable.misstress_6,
        R.drawable.misstress_4,
        R.drawable.cosplay_13,
        R.drawable.days_5,
        R.drawable.days_4,
        R.drawable.days_3,
        R.drawable.days_2,
        R.drawable.days_1,
        R.drawable.morning_4,
        R.drawable.cosplay_12,
        R.drawable.cosplay_11,
        R.drawable.cosplay_10,
        R.drawable.cosplay_9,
        R.drawable.cosplay_8,
        R.drawable.cosplay_7,
        R.drawable.cosplay_6,
        R.drawable.sakura_5,
        R.drawable.villiage_1,
        R.drawable.sakura_4,
        R.drawable.city_7,
        R.drawable.morning_3,
        R.drawable.work_2,
        R.drawable.cosplay_4,
        R.drawable.autmn__1,
        R.drawable.face_1,
        R.drawable.evening_3,
        R.drawable.city_6,
        R.drawable.dark_1,
        R.drawable.evening_2,
        R.drawable.piano_3,
        R.drawable.cosplay_3,
        R.drawable.forest_1,
        R.drawable.party_4,
        R.drawable.work_1,
        R.drawable.cosplay_2,
        R.drawable.bitch_7,
        R.drawable.sport_2,
        R.drawable.teller_1,
        R.drawable.horse_1,
        R.drawable.bitch_6,
        R.drawable.bitch_5,
        R.drawable.piano_2,
        R.drawable.bicikle_1,
        R.drawable.city_5,
        R.drawable.shop_2,
        R.drawable.evening_1,
        R.drawable.garden_1,
        R.drawable.party_3,
        R.drawable.book_3,
        R.drawable.book_2,
        R.drawable.snow_2,
        R.drawable.piano_1,
        R.drawable.cosplay_1,
        R.drawable.city_4,
        R.drawable.city_3,
        R.drawable.city_2,
        R.drawable.city_1,
        R.drawable.book_1,
        R.drawable.bitch_4,
        R.drawable.bitch_3,
        R.drawable.autmn_2,
        R.drawable.morning_2,
        R.drawable.party_2,
        R.drawable.shop_1,
        R.drawable.sakura_3,
        R.drawable.sport_1,
        R.drawable.party_1,
        R.drawable.snow_1,
        R.drawable.paris_1,
        R.drawable.morning_1,
        R.drawable.bitch_2,
        R.drawable.sakura_2,
        R.drawable.bitch_1,
        R.drawable.city_9,
        R.drawable.auto_1,
        R.drawable.cosplay_14,
        R.drawable.city_8,
        R.drawable.evening_5,
        R.drawable.evening_4,
        R.drawable.zima_1,
        R.drawable.most_1,
        R.drawable.moning_5,
        // ... добавь все свои картинки
    )

    init {
        require(imageList.isNotEmpty()) { "Добавь хотя бы одну картинку в imageList" }
    }

    // Поток текущей картинки
    val currentImage: Flow<Int> = context.dataStore.data
        .map { prefs ->
            prefs[LAST_IMAGE_INDEX] ?: 0
        }
        .map { index ->
            if (index in imageList.indices) imageList[index] else imageList[0]
        }

    // Выбираем новую случайную картинку и сохраняем её индекс
    suspend fun updateImage() {
        // Получаем текущий индекс из DataStore (одним запросом)
        val currentIndex = context.dataStore.data.map { it[LAST_IMAGE_INDEX] ?: 0 }.first()

        var newIndex: Int
        do {
            newIndex = (imageList.indices).random()
        } while (newIndex == currentIndex && imageList.size > 1) // Перевыбираем, если совпало

        context.dataStore.edit { prefs ->
            prefs[LAST_IMAGE_INDEX] = newIndex
        }
    }
}