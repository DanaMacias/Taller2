package com.example.taller2.data.model

data class GameState(
    val roomCode: String = "",
    val players: List<String> = emptyList(),
    val currentTurnIndex: Int = 0,
    val currentRound: Int = 1,
    val timerSeconds: Int = 60,
    val isPaused: Boolean = false,
    val messages: List<ChatMessage> = emptyList()
) {
    val currentPlayer: String
        get() = if (players.isNotEmpty()) players[currentTurnIndex] else ""
}


