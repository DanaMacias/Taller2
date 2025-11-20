package com.example.taller2.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taller2.data.model.JoinResult
import com.example.taller2.data.model.Room
import com.example.taller2.data.repository.RoomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.Boolean

class RoomViewModel(
    private val repository: RoomRepository = RoomRepository()
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

    fun closeRoom(roomCode: String, onComplete: (Boolean) -> Unit) {
        repository.closeRoom(roomCode, onComplete)
    }

    fun leaveRoom(roomCode: String, userId: String, onComplete: (Boolean) -> Unit) {
        repository.leaveRoom(roomCode, userId, onComplete)
    }

    fun clearRoomState() {
        _room.value = null
    }

    fun startGame(roomId: String, onComplete: (Boolean) -> Unit) {
        repository.startGame(roomId, onComplete)
    }
}

