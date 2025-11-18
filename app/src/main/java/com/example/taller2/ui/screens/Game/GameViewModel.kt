package com.example.taller2.ui.screens.Game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taller2.data.repository.GameRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay

class GameViewModel(
    private val repository: GameRepository
) : ViewModel() {

    private val _state = MutableStateFlow(GameState())
    val state = _state.asStateFlow()

    private var timerJob: Job? = null

    fun loadGame(roomCode: String) {
        viewModelScope.launch {
            repository.listenGame(roomCode).collect { firebaseState ->
                firebaseState?.let {
                    _state.value = it
                }
            }
        }
    }

    fun startTurnCountdown(roomCode: String) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            for (sec in 60 downTo 0) {
                delay(1000)
                if (_state.value.isPaused) continue

                _state.update { it.copy(timerSeconds = sec) }
                repository.updateTimer(roomCode, sec)

                if (sec == 0) {
                    nextTurn(roomCode)
                }
            }
        }
    }

    fun pause() {
        _state.update { it.copy(isPaused = true) }
    }

    fun resume() {
        _state.update { it.copy(isPaused = false) }
    }

    private fun nextTurn(roomCode: String) {
        val s = _state.value

        val isLastTurn = s.currentTurnIndex == s.players.lastIndex
        val newTurnIndex = if (isLastTurn) 0 else s.currentTurnIndex + 1
        val newRound = if (isLastTurn) s.currentRound + 1 else s.currentRound

        val updated = s.copy(
            currentTurnIndex = newTurnIndex,
            currentRound = newRound,
            timerSeconds = 60
        )

        _state.value = updated
        repository.updateGameState(roomCode, updated)

        startTurnCountdown(roomCode)
    }
}
