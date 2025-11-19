package com.example.taller2.data.repository

import com.example.taller2.data.model.GameState
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

class GameRepository {

    private val db = FirebaseDatabase.getInstance().reference

    // ----------------------- LECTURA DEL ESTADO -----------------------
    fun listenGame(roomCode: String) = callbackFlow {
        val ref = db.child("rooms").child(roomCode).child("game")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val state = snapshot.getValue(GameState::class.java)
                trySend(state)
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }


    // ----------------------- ACTUALIZAR ESTADO COMPLETO -----------------------
    fun updateGame(roomCode: String, gameState: GameState) {
        db.child("rooms/$roomCode/game").setValue(gameState)
    }


    // ----------------------- TIMER -----------------------
    fun updateTimer(roomCode: String, sec: Int) {
        db.child("rooms/$roomCode/game/timerSeconds").setValue(sec)
    }

    fun updatePause(roomCode: String, paused: Boolean) {
        db.child("rooms/$roomCode/game/isPaused").setValue(paused)
    }


    // ----------------------- GANADOR / EMPATE -----------------------
    fun markWinner(roomCode: String, player: String) {
        db.child("rooms/$roomCode/game/winner").setValue(player)
    }

    fun markDraw(roomCode: String) {
        db.child("rooms/$roomCode/game/winner").setValue("EMPATE")
    }


    // ----------------------- CHAT -----------------------
    fun sendMessage(roomCode: String, text: String) {
        val messageId = db.push().key ?: return

        val chatRef = db.child("rooms/$roomCode/game/chat/$messageId")

        val message = mapOf(
            "text" to text,
            "timestamp" to System.currentTimeMillis()
        )

        chatRef.setValue(message)
    }
}
