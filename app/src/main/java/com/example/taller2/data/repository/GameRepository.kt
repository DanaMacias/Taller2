package com.example.taller2.data.repository

import com.example.taller2.data.firebase.FirebaseDataSource
import com.example.taller2.data.model.ChatMessage
import com.example.taller2.data.model.GameState
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

class GameRepository(private val firebase: FirebaseDataSource = FirebaseDataSource()) {

    private fun gameRef(roomId: String) = firebase.roomsRef().child(roomId).child("game")
    private fun chatRef(roomId: String) = firebase.roomsRef().child(roomId).child("chat")

    fun createInitialGameState(roomId: String, gameState: GameState, onComplete: (Boolean) -> Unit) {
        // Escribe el estado inicial del juego en la sub-colección "game"
        gameRef(roomId).setValue(gameState)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun startGameWrite(roomId: String, info: GameState, onComplete: (Boolean) -> Unit) {
        // Referencia a la SALA completa, no solo al juego
        // Nota: firebase.roomsRef() debe ser accesible aquí
        val roomRef = firebase.roomsRef().child(roomId)

        // Preparamos una actualización atómica (todo o nada)
        val updates = mapOf(
            "game" to info,         // 1. Guarda los datos (emojis, turnos)
            "gameStarted" to true   // 2. Avisa a todos que el juego inició
        )

        // updateChildren asegura que ambos campos se escriban exactamente al mismo tiempo
        roomRef.updateChildren(updates)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }
    fun updateGameFields(roomId: String, updates: Map<String, Any?>, onComplete: (Boolean)->Unit) {
        gameRef(roomId).updateChildren(updates)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun setTurn(roomId: String, index: Int, deadlineMillis: Long, onComplete: (Boolean)->Unit) {
        updateGameFields(roomId, mapOf("currentTurnIndex" to index, "turnDeadline" to deadlineMillis), onComplete)
    }

    fun addGuess(roomId: String, playerId: String, guessedEmoji: String, onComplete: (Boolean)->Unit) {
        val key = "/guesses/$playerId"
        updateGameFields(roomId, mapOf(key to guessedEmoji), onComplete)
    }

    fun sendChatMessage(roomId: String, msg: ChatMessage, onComplete: (Boolean)->Unit) {
        val ref = chatRef(roomId).push()
        val m = msg.copy(id = ref.key ?: "")
        ref.setValue(m).addOnSuccessListener { onComplete(true) }.addOnFailureListener { onComplete(false) }
    }

    fun listenGame(roomId: String) = callbackFlow<GameState?> {
        val ref = gameRef(roomId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val gi = snapshot.getValue(GameState::class.java)
                    trySend(gi)
                } catch (e: Exception) {
                    // Log error if needed
                    trySend(null)
                }
            }
            override fun onCancelled(error: DatabaseError) { trySend(null) }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun listenChat(roomId: String) = callbackFlow<List<ChatMessage>> {
        val ref = chatRef(roomId)
        val listener = object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<ChatMessage>()
                snapshot.children.forEach { ch ->
                    val m = ch.getValue(ChatMessage::class.java)
                    if (m != null) list.add(m)
                }
                trySend(list)
            }
            override fun onCancelled(error: DatabaseError) { trySend(emptyList()) }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }
}