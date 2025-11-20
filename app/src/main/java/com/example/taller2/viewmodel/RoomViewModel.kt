package com.example.taller2.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taller2.data.model.GameState
import com.example.taller2.data.model.JoinResult
import com.example.taller2.data.model.Room
import com.example.taller2.data.repository.GameRepository
import com.example.taller2.data.repository.RoomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RoomViewModel(
    private val repository: RoomRepository = RoomRepository(),
    private val gameRepository: GameRepository = GameRepository() // 1. Inyectar GameRepository
) : ViewModel() {

    private val _room = MutableStateFlow<Room?>(null)
    val room: StateFlow<Room?> = _room

    fun generateRoomCode(onGenerated: (String) -> Unit) {
        repository.generateUniqueRoomCode(onGenerated)
    }

    fun createRoom(roomCode: String, userId: String, userName: String, onComplete: (Boolean) -> Unit) {
        val room = Room(
            id = roomCode,
            hostId = userId,
            players = mapOf(userId to userName),
            playerStatus = mapOf(userId to false),
            isActive = true,
            currentTurnPlayerId = userId,
            maxPlayers = 4,
            gameStarted = false
        )

        repository.createRoom(room) { success ->
            onComplete(success)
        }
    }

    fun joinRoom(roomCode: String, userId: String, userName: String, onComplete: (JoinResult) -> Unit) {
        repository.joinRoom(roomCode, userId, userName, onComplete)
    }

    fun startListening(roomCode: String) {
        viewModelScope.launch {
            repository.listenRoom(roomCode).collect { updatedRoom ->
                _room.value = updatedRoom
            }
        }
    }

    fun deleteRoom(roomCode: String, onComplete: (Boolean) -> Unit) {
        repository.deleteRoom(roomCode, onComplete)
    }

    fun leaveRoom(roomCode: String, userId: String, onComplete: (Boolean) -> Unit) {
        repository.leaveRoom(roomCode, userId, onComplete)
    }

    fun clearRoomState() {
        _room.value = null
    }

    /**
     * Inicia la partida. Esta función ahora tiene la responsabilidad de crear el estado
     * inicial del juego ANTES de notificar a los clientes que la partida ha comenzado.
     */
    fun startGame(roomId: String, onComplete: (Boolean) -> Unit) {
        val currentRoom = _room.value
        // Asegurarse de que el estado de la sala está cargado y hay jugadores suficientes.
        if (currentRoom == null || currentRoom.players.size < 2) {
            onComplete(false)
            return
        }

        // 2. Crear el objeto GameState inicial.
        val playersOrder = currentRoom.players.keys.toList().shuffled()
        val emojis = listOf("😀","😜","😎","🤖","🐱","🍕","🏀","🌈","🐶","🦄").shuffled()
        val assignedEmojis = playersOrder.mapIndexed { index, playerId ->
            playerId to emojis[index % emojis.size]
        }.toMap()
        val turnDurationMillis = 60_000L

        val initialGameState = GameState(
            started = true,
            assignedEmojis = assignedEmojis,
            playersOrder = playersOrder,
            currentTurnIndex = 0,
            turnDeadline = System.currentTimeMillis() + turnDurationMillis,
            guesses = emptyMap(),
            round = 1
        )

        // 3. Guardar el GameState en la base de datos.
        gameRepository.createInitialGameState(roomId, initialGameState) { gameStateSuccess ->
            if (gameStateSuccess) {
                // 4. Si la creación del GameState fue exitosa, actualizar la sala para que todos naveguen.
                repository.updateRoomField(roomId, "gameStarted", true) { roomUpdateSuccess ->
                    onComplete(roomUpdateSuccess)
                }
            } else {
                // Si falla la creación del GameState, no se inicia la partida.
                onComplete(false)
            }
        }
    }
}