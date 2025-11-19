package com.example.taller2.ui.screens.Register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taller2.data.model.Player
import com.example.taller2.data.repository.PlayerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val repo: PlayerRepository = PlayerRepository()
) : ViewModel() {

    private val _registerSuccess = MutableStateFlow(false)
    val registerSuccess: StateFlow<Boolean> = _registerSuccess

    private val _registerError = MutableStateFlow<String?>(null)
    val registerError: StateFlow<String?> = _registerError


    fun setError(message: String) {
        _registerError.value = message
    }

    fun clearError() {
        _registerError.value = null
    }


    fun register(fullName: String, email: String, password: String) {
        viewModelScope.launch {


            val player = Player(
                id = "",
                name = fullName.trim(),
                email = email.trim(),
                password = password,

            )


            repo.addPlayer(player)


            _registerSuccess.value = true

        }
    }
}
