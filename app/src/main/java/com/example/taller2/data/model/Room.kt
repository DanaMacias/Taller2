package com.example.taller2.data.model

data class Room(
    val id: String = "",
    val hostPlayer: String = "",
    val players: List<String> = emptyList(),
    val isActive: Boolean = true,
    val playerTurn: String? = null
)
