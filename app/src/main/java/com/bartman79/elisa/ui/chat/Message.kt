package com.bartman79.elisa.ui.chat

data class Message(
    val text: String,
    val isUser: Boolean, // true – от пользователя, false – от Элизы
    val timestamp: Long = System.currentTimeMillis()
)