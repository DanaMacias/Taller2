package com.example.taller2.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taller2.data.model.Player
import com.example.taller2.data.repository.PlayerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val repository: PlayerRepository = PlayerRepository()
) : ViewModel() {

    private val _players = MutableStateFlow<List<Player>>(emptyList())
    val players: StateFlow<List<Player>> = _players

    init {
        viewModelScope.launch {
            repository.getPlayers().collect { list ->
                _players.value = list
            }
        }
    }

    fun addPlayer(player: Player) = repository.addPlayer(player)
}