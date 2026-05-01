package com.bartman79.elisa.ui.encouragements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlin.random.Random

class EncouragementsViewModel(
    initialPhrases: Array<String>
) : ViewModel() {

    private var phrases: Array<String> = initialPhrases
    private var currentPhrase: String = phrases[Random.nextInt(phrases.size)]

    fun getCurrentPhrase(): String = currentPhrase

    fun updatePhrases(newPhrases: Array<String>) {
        phrases = newPhrases
        currentPhrase = phrases.random()
    }

    fun nextPhrase() {
        currentPhrase = phrases[Random.nextInt(phrases.size)]
    }

    class Factory(private val phrases: Array<String>) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EncouragementsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return EncouragementsViewModel(phrases) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}