package com.example.taller2.ui.screens.Account

import android.util.Log
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
        // 1. Imprimir qué ID estamos buscando
        Log.d("AccountDebug", "Buscando usuario con ID: '$userId'")

        if (userId.isBlank()) {
            Log.e("AccountDebug", "ERROR: El userId está vacío. Revisa el Login.")
            return
        }

        viewModelScope.launch {
            repository.getPlayers().collect { players ->
                // 2. Ver cuántos jugadores descargamos
                Log.d("AccountDebug", "Se descargaron ${players.size} jugadores de Firebase")

                val foundUser = players.find { it.id == userId }

                if (foundUser != null) {
                    Log.d("AccountDebug", "¡Usuario encontrado!: ${foundUser.name}")
                    _player.value = foundUser
                } else {
                    Log.e("AccountDebug", "No se encontró ningún jugador con el ID: $userId")
                    // Opcional: Aquí podrías poner un estado de error para quitar el spinner
                }
            }
        }
    }
}