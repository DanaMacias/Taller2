package com.example.taller2.data.model

data class GameState(
    val started: Boolean = false,
    val assignedEmojis: Map<String, String> = emptyMap(),
    val playersOrder: List<String> = emptyList(),
    val currentTurnIndex: Int = 0,
    val turnDeadline: Long = 0L,
    val guesses: Map<String, String> = emptyMap(), // Mapa de qué adivinó cada uno en ESTA ronda
    val round: Int = 1,
    val eliminatedPlayers: List<String> = emptyList(), // IDs de jugadores muertos
    val isFinalRound: Boolean = false,                 // Bandera para la última ronda de los 2 finalistas
    val winnerId: String? = null,                      // ID del ganador, "DRAW" para empate, o null si sigue jugando
    val gameEnded: Boolean = false                     // Bandera final total
)
