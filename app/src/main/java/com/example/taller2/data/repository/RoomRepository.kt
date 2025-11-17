package com.example.taller2.data.repository

import com.example.taller2.data.model.Room
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class RoomRepository {

    private val db = FirebaseDatabase.getInstance().reference

    fun roomsRef() = db.child("rooms")

    fun generateUniqueRoomCode(onGenerated: (String) -> Unit) {
        val code = (1000..9999).random().toString()
        onGenerated(code)
    }

    fun createRoomWithPlayers(roomCode: String, host: String, onComplete: (Boolean) -> Unit) {
        val room = Room(
            id = roomCode,
            hostPlayer = host,
            players = listOf(host),
            isActive = true,
            playerTurn = host
        )

        roomsRef().child(roomCode).setValue(room)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun joinRoom(roomCode: String, player: String, onComplete: (Boolean) -> Unit) {
        val roomRef = roomsRef().child(roomCode)

        roomRef.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                onComplete(false)
                return@addOnSuccessListener
            }

            val room = snapshot.getValue(Room::class.java) ?: return@addOnSuccessListener

            if (room.players.contains(player)) {
                onComplete(true)
                return@addOnSuccessListener
            }

            if (room.players.size >= 4) {
                onComplete(false)
                return@addOnSuccessListener
            }

            val updatedPlayers = room.players + player

            val updates = mapOf(
                "players" to updatedPlayers,
                "playerTurn" to room.hostPlayer
            )

            roomRef.updateChildren(updates)
                .addOnSuccessListener { onComplete(true) }
                .addOnFailureListener { onComplete(false) }

        }.addOnFailureListener {
            onComplete(false)
        }
    }

    fun listenRoom(roomCode: String): Flow<Room?> = callbackFlow {
        val roomRef = roomsRef().child(roomCode)

        val listener = roomRef.addValueEventListener(
            object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    trySend(snapshot.getValue(Room::class.java))
                }

                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
            }
        )

        awaitClose { roomRef.removeEventListener(listener) }
    }
}
