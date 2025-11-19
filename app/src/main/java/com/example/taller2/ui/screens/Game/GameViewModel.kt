package com.example.taller2.ui.screens.Game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taller2.data.model.GameState
import com.example.taller2.data.repository.GameRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameViewModel(
    private val repository: GameRepository = GameRepository()
) : ViewModel() {

    private val _gameState = MutableStateFlow<GameState?>(null)
    val gameState = _gameState.asStateFlow()

    private var timerJob: Job? = null


    fun startListening(roomCode: String) {
        viewModelScope.launch {
            repository.listenGame(roomCode).collect { state ->
                _gameState.value = state
            }
        }
    }


    fun initializeGame(roomCode: String, players: List<String>) {

        val initialState = GameState(
            players = players,
            currentTurnIndex = 0,
            currentRound = 1,
            timerSeconds = 60,
            isPaused = false
        )

        repository.updateGame(roomCode, initialState)

        startTurnCountdown(roomCode)
    }


    fun startTurnCountdown(roomCode: String) {

        timerJob?.cancel()

        timerJob = viewModelScope.launch {

            for (sec in 60 downTo 0) {

                val state = _gameState.value ?: return@launch

                if (!state.isPaused) {
                    repository.updateTimer(roomCode, sec)
                }

                delay(1000)

                if (sec == 0) {
                    nextTurn(roomCode)
                }
            }
        }
    }


    fun pauseGame() {
        val roomCode = _gameState.value?.roomCode ?: return
        repository.updatePause(roomCode, true)
    }

    fun resumeGame() {
        val roomCode = _gameState.value?.roomCode ?: return
        repository.updatePause(roomCode, false)
    }


    fun nextTurn(roomCode: String) {
        val state = _gameState.value ?: return

        val players = state.players

        if (players.isEmpty()) return

        val nextIndex =
            if (state.currentTurnIndex >= players.lastIndex) 0
            else state.currentTurnIndex + 1

        val nextRound =
            if (nextIndex == 0) state.currentRound + 1 else state.currentRound

        val updated = state.copy(
            currentTurnIndex = nextIndex,
            currentRound = nextRound,
            timerSeconds = 60
        )

        repository.updateGame(roomCode, updated)

        startTurnCountdown(roomCode)
    }


    fun eliminatePlayer(player: String) {
        val state = _gameState.value ?: return
        val roomCode = state.roomCode ?: return

        val newPlayers = state.players.filterNot { it == player }

        val updated = state.copy(players = newPlayers)

        repository.updateGame(roomCode, updated)

        evaluateIfGameEnds(roomCode, updated)
    }


    private fun evaluateIfGameEnds(roomCode: String, state: GameState) {

        when (state.players.size) {
            0 -> repository.markDraw(roomCode)
            1 -> repository.markWinner(roomCode, state.players.first())
            2 -> {
                if (state.currentRound > 1) {
                    repository.markDraw(roomCode)
                }
            }
        }
    }


    fun sendChatMessage(text: String) {
        val roomCode = _gameState.value?.roomCode ?: return
        repository.sendMessage(roomCode, text)
    }
}
