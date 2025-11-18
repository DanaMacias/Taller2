package com.example.taller2.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class GameRepository(private val db: FirebaseDatabase) {

    fun listenGame(roomCode: String) = callbackFlow {
        val ref = db.getReference("rooms/$roomCode/game")

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

    fun updateGameState(roomCode: String, state: GameState) {
        db.getReference("rooms/$roomCode/game").setValue(state)
    }

    fun updateTimer(roomCode: String, seconds: Int) {
        db.getReference("rooms/$roomCode/game/timerSeconds").setValue(seconds)
    }
}
