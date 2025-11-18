package com.example.taller2.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taller2.data.model.Room
import com.example.taller2.data.repository.RoomRepository


import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RoomViewModel(
    private val repository: RoomRepository = RoomRepository()
) : ViewModel() {

    private val _room = MutableStateFlow<Room?>(null)
    val room: StateFlow<Room?> = _room

    fun generateRoomCode(onGenerated: (String) -> Unit) {
        repository.generateUniqueRoomCode(onGenerated)
    }

    fun createRoom(roomCode: String, host: String, onComplete: (Boolean) -> Unit) {
        repository.createRoomWithPlayers(roomCode, host) { success ->
            onComplete(success)
        }
    }

    fun joinRoom(roomCode: String, player: String, onComplete: (Boolean) -> Unit) {
        repository.joinRoom(roomCode, player) { success ->
            onComplete(success)
        }
    }

    fun startListening(roomCode: String) {
        viewModelScope.launch {
            repository.listenRoom(roomCode).collect { updatedRoom ->
                _room.value = updatedRoom
            }
        }
    }
}
