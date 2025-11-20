package com.example.taller2.data.model

import com.google.firebase.database.PropertyName

data class Room(
    val id: String = "",
    val hostId: String = "",
    val players: Map<String, String> = emptyMap(),
    val playerStatus: Map<String, Boolean> = emptyMap(),
    @get:PropertyName("isActive")
    @set:PropertyName("isActive")
    var isActive: Boolean = true,
    val currentTurnPlayerId: String? = null,
    val maxPlayers: Int = 4,
    val gameStarted: Boolean = false
)