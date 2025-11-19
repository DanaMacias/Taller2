package com.example.taller2.ui.screens.Account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taller2.data.model.Player
import com.example.taller2.data.repository.PlayerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AccountViewModel(
    private val repository: PlayerRepository = PlayerRepository()
) : ViewModel() {

    private val _player = MutableStateFlow<Player?>(null)
    val player: StateFlow<Player?> = _player

    fun loadUser(userId: String) {
        viewModelScope.launch {
            repository.getPlayers().collect { players ->
                _player.value = players.find { it.id == userId }
            }
        }
    }
}
