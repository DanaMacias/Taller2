package com.example.taller2.data.model

import androidx.annotation.Keep

@Keep // ✅ Añadido para proteger la clase durante la compilación y asegurar la deserialización de Firebase.
data class GameState(
    val started: Boolean = false,
    val assignedEmojis: Map<String, String> = emptyMap(), // playerId -> emoji
    val playersOrder: List<String> = emptyList(), // playerIds in order
    val currentTurnIndex: Int = 0,
    val turnDeadline: Long = 0L, // epoch millis cuando termina el turno actual
    val guesses: Map<String, String> = emptyMap(), // playerId -> emoji adivinado
    val round: Int = 0
)
