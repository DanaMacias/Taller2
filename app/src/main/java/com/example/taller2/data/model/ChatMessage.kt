package com.example.taller2.data.model

data class ChatMessage(
    val text: String = "",
    val sender: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

