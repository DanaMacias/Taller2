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

    fun startListening(roomId: String) {
        // 1. Escuchar datos de la Sala (Jugadores, Host, Estado general)
        viewModelScope.launch {
            roomRepository.listenRoom(roomId).collect { _room.value = it }
        }
        // 2. Escuchar datos del Juego (Turnos, Emojis, Adivinanzas)
        viewModelScope.launch {
            gameRepository.listenGame(roomId).collect { gi ->
                _gameState.value = gi
            }
        }
        // 3. Escuchar el Chat
        viewModelScope.launch {
            gameRepository.listenChat(roomId).collect { msgs ->
                _chat.value = msgs.sortedBy { it.timestamp }
            }
        }

        // 4. Iniciar el reloj visual
        startLocalTimer()
    }

    /**
     * Bucle infinito que actualiza _timeRemaining basándose en la hora del servidor.
     * NO depende de la memoria del teléfono, sino de la diferencia con 'turnDeadline'.
     */
    private fun startLocalTimer() {
        viewModelScope.launch {
            while (true) {
                val state = _gameState.value

                // Solo calculamos tiempo si el juego ya inició
                if (state != null && state.started) {
                    val now = System.currentTimeMillis()

                    // Cálculo: (Tiempo límite - Ahora) / 1000
                    val diff = ((state.turnDeadline - now) / 1000).coerceAtLeast(0)

                    _timeRemaining.value = diff.toInt()
                }
                delay(1000) // Espera 1 segundo exacto
            }
        }
    }


    // --- ACCIONES DEL JUEGO ---

    /**
     * Lógica del Anfitrión para preparar y lanzar la partida.
     */
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

    /**
     * El jugador activo intenta adivinar su emoji.
     */
    fun submitGuess(roomId: String, playerId: String, guessedEmoji: String) {
        // Validación local rápida
        val correctEmoji = _gameState.value?.assignedEmojis?.get(playerId)
        val isCorrect = correctEmoji == guessedEmoji

        if (isCorrect) {
            // Si acierta, guardamos en Firebase
            gameRepository.addGuess(roomId, playerId, guessedEmoji) { ok ->
                if (ok) {
                    // Si se guardó bien, pasamos al siguiente turno
                    advanceTurnIfNeeded(roomId)
                }
            }
        } else {
            // Si falla: Podrías agregar lógica aquí (ej. quitar tiempo, mostrar mensaje)
            // Por ahora no hacemos nada, el usuario debe volver a intentar.
        }
    }

    /**
     * Calcula quién sigue y actualiza el turno en Firebase.
     */
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