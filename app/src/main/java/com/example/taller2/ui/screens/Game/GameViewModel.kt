package com.example.taller2.ui.screens.Game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taller2.data.model.ChatMessage
import com.example.taller2.data.model.GameState
import com.example.taller2.data.model.Room
import com.example.taller2.data.repository.GameRepository
import com.example.taller2.data.repository.RoomRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class GameViewModel(
    private val roomRepository: RoomRepository = RoomRepository(),
    private val gameRepository: GameRepository = GameRepository()
) : ViewModel() {

    private val _gameState = MutableStateFlow<GameState?>(null)
    val gameState: StateFlow<GameState?> = _gameState

    private val _room = MutableStateFlow<Room?>(null)
    val room: StateFlow<Room?> = _room

    val playerNames = combine(gameState, room) { _, roomData ->
        roomData?.players ?: emptyMap()
    }

    private val _chat = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chat = _chat.asStateFlow()

    private val _timeRemaining = MutableStateFlow(60)
    val timeRemaining = _timeRemaining.asStateFlow()

    val availableEmojis = listOf("😀","😜","😎","🤖","🐱","🍕","🏀","🌈","🐶","🦄")

    private var myPlayerId: String = ""
    private var currentRoomId: String = ""

    fun setPlayerId(id: String) { myPlayerId = id }

    fun startListening(roomId: String) {
        currentRoomId = roomId
        viewModelScope.launch { roomRepository.listenRoom(roomId).collect { _room.value = it } }
        viewModelScope.launch { gameRepository.listenGame(roomId).collect { _gameState.value = it } }
        viewModelScope.launch { gameRepository.listenChat(roomId).collect { _chat.value = it } }
        startLocalTimer()
    }

    // --- TIMER CON LÓGICA DE ELIMINACIÓN POR TIEMPO ---
    private fun startLocalTimer() {
        viewModelScope.launch {
            while (true) {
                val state = _gameState.value
                val roomData = _room.value

                if (state != null && state.started && !state.gameEnded && roomData != null) {
                    val now = System.currentTimeMillis()
                    val diff = ((state.turnDeadline - now) / 1000).coerceAtLeast(0)
                    _timeRemaining.value = diff.toInt()

                    // Si soy el HOST y se acaba el tiempo:
                    if (diff <= 0L && roomData.hostId == myPlayerId) {
                        // El jugador actual NO adivinó a tiempo -> Eliminado
                        handleTimeOut(currentRoomId, state)
                        delay(2000)
                    }
                }
                delay(1000)
            }
        }
    }
    fun submitGuess(roomId: String, playerId: String, guessedEmoji: String) {
        val state = _gameState.value ?: return
        val correctEmoji = state.assignedEmojis[playerId]

        // Validar si es correcto
        if (correctEmoji == guessedEmoji) {
            // ACERTÓ: Guardamos la adivinanza
            gameRepository.addGuess(roomId, playerId, guessedEmoji) { ok ->
                if (ok) advanceTurnLogic(roomId) // Pasa turno
            }
        } else {
            // FALLÓ: Eliminar jugador inmediatamente
            eliminateAndAdvance(roomId, playerId, state)
        }
    }

    // Manejo de Timeout (Se acaba el tiempo sin adivinar)
    private fun handleTimeOut(roomId: String, state: GameState) {
        val currentPlayerId = state.playersOrder.getOrNull(state.currentTurnIndex) ?: return
        // Si se le acaba el tiempo, cuenta como fallo -> Eliminar
        eliminateAndAdvance(roomId, currentPlayerId, state)
    }

    private fun eliminateAndAdvance(roomId: String, playerId: String, state: GameState) {
        val newEliminatedList = state.eliminatedPlayers.toMutableList().apply { add(playerId) }

        gameRepository.updateGameFields(roomId, mapOf("eliminatedPlayers" to newEliminatedList)) { ok ->
            if (ok) {
                sendSystemMessage(roomId, "🚫 El jugador se equivocó y ha sido ELIMINADO.")
                advanceTurnLogic(roomId)
            }
        }
    }

    fun advanceTurnLogic(roomId: String) {

        val state = _gameState.value ?: return
        val totalPlayers = state.playersOrder.size
        var nextIndex = state.currentTurnIndex + 1

        while (nextIndex < totalPlayers && state.eliminatedPlayers.contains(state.playersOrder[nextIndex])) {
            nextIndex++
        }

        if (nextIndex >= totalPlayers) {
            evaluateEndOfRound(roomId, state)
        } else {
            val now = System.currentTimeMillis()
            val newDeadline = now + 60_000L
            gameRepository.setTurn(roomId, nextIndex, newDeadline) { }
        }
    }

    private fun evaluateEndOfRound(roomId: String, state: GameState) {
        val activePlayers = state.playersOrder.filter { !state.eliminatedPlayers.contains(it) }
        val activeCount = activePlayers.size

        if (activeCount < 2) {
            val winner = activePlayers.firstOrNull() ?: "Nadie"
            gameRepository.endGame(roomId, winner) { }
            return
        }

        if (state.isFinalRound) {
            determineWinnerOfFinalRound(roomId, state, activePlayers)
        } else {
            if (activeCount == 2) {
                sendSystemMessage(roomId, "🔥 ¡DUELO FINAL! Solo quedan 2 jugadores.")
                startNewRound(roomId, state.round + 1, isFinal = true)
            } else {
                startNewRound(roomId, state.round + 1, isFinal = false)
            }
        }
    }

    private fun startNewRound(roomId: String, roundNum: Int, isFinal: Boolean) {
        val now = System.currentTimeMillis()
        val deadline = now + 60_000L

        // IMPORTANTE: currentTurnIndex debe empezar en el primer jugador NO eliminado
        val state = _gameState.value ?: return
        var firstActiveIndex = 0
        while (firstActiveIndex < state.playersOrder.size &&
            state.eliminatedPlayers.contains(state.playersOrder[firstActiveIndex])) {
            firstActiveIndex++
        }

        val updates = mapOf(
            "currentTurnIndex" to firstActiveIndex,
            "round" to roundNum,
            "guesses" to null, // Limpiar adivinanzas
            "isFinalRound" to isFinal,
            "turnDeadline" to deadline
        )
        gameRepository.updateGameFields(roomId, updates) { }
    }

    private fun determineWinnerOfFinalRound(roomId: String, state: GameState, activePlayers: List<String>) {
        gameRepository.endGame(roomId, "DRAW") { }
    }

    private fun sendSystemMessage(roomId: String, text: String) {
        gameRepository.sendChatMessage(roomId, ChatMessage("SYS", "JUEGO", text)) {}
    }
    fun hostStartGame(roomId: String, onComplete: (Boolean) -> Unit) {
        val playersMap = _room.value?.players ?: return onComplete(false)
        val playersOrder = playersMap.keys.toList().shuffled()

        // 1. Asignar emojis aleatorios
        val shuffledEmojis = availableEmojis.shuffled()
        val assigned = playersOrder.mapIndexed { idx, pid ->
            pid to shuffledEmojis[idx % shuffledEmojis.size]
        }.toMap()

        // 2. Configurar tiempos
        val now = System.currentTimeMillis()
        val turnDurationMillis = 60_000L // 1 minuto
        val deadline = now + turnDurationMillis

        // 3. Crear objeto GameState inicial
        val info = GameState(
            started = true,
            assignedEmojis = assigned,
            playersOrder = playersOrder,
            currentTurnIndex = 0,
            turnDeadline = deadline,
            guesses = emptyMap(),
            round = 1
        )

        // 4. Escribir en Firebase (Esto también pone gameStarted = true automáticamente en el repo)
        gameRepository.startGameWrite(roomId, info) { ok ->
            onComplete(ok)
        }
    }

    fun advanceTurnIfNeeded(roomId: String) {
        val gi = _gameState.value ?: return
        val totalPlayers = gi.playersOrder.size
        val nextIndex = gi.currentTurnIndex + 1

        if (nextIndex >= totalPlayers) {
            // Ya pasaron todos los jugadores -> Fin del Juego
            gameRepository.updateGameFields(roomId, mapOf("started" to false)) { }
        } else {
            // Siguiente turno -> Calculamos nuevo tiempo límite
            val now = System.currentTimeMillis()
            val newDeadline = now + 60_000L // 1 minuto más

            gameRepository.setTurn(roomId, nextIndex, newDeadline) { }
        }
    }

    /**
     * Envía mensajes al chat de la sala.
     */
    fun sendChatMessage(roomId: String, senderId: String, senderName: String, text: String) {
        if (text.isBlank()) return

        val msg = ChatMessage(
            senderId = senderId,
            senderName = senderName,
            text = text
        )
        gameRepository.sendChatMessage(roomId, msg) { }
    }
}