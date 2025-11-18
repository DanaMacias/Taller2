package com.example.taller2.ui.screens.login



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taller2.data.model.Player
import com.example.taller2.data.repository.PlayerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repo: PlayerRepository = PlayerRepository()
) : ViewModel() {

    private val _loginResult = MutableStateFlow<Player?>(null)
    val loginResult: StateFlow<Player?> = _loginResult

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError

    fun login(username: String, password: String) {
        val cleanUser = username.trim()
        val cleanPass = password.trim()

        viewModelScope.launch {
            repo.login(cleanUser, cleanPass).collect { user ->


                if (user != null) {
                    _loginResult.value = user
                    _loginError.value = null
                } else {
                    _loginError.value = "Credenciales incorrectas"
                }
            }
        }
    }
}

