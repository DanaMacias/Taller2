package com.example.taller2.data.repository

import com.example.taller2.data.firebase.FirebaseDataSource
import com.example.taller2.data.model.Room
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

class RoomRepository(
    private val firebase: FirebaseDataSource = FirebaseDataSource()
) {

    fun generateUniqueRoomCode(onGenerated: (String) -> Unit) {
        val code = (1000..9999).random().toString()

        firebase.roomsRef().child(code).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                generateUniqueRoomCode(onGenerated) // vuelve a intentar
            } else {
                onGenerated(code)
            }
        }
    }

    fun createRoom(room: Room, onComplete: (Boolean) -> Unit) {
        val roomRef = firebase.roomsRef().child(room.id)

        roomRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                onComplete(false) // ya existe
                return@addOnSuccessListener
            }

            roomRef.setValue(room)
                .addOnCompleteListener { task ->
                    onComplete(task.isSuccessful)
                }

        }.addOnFailureListener {
            onComplete(false)
        }
    }

    fun joinRoom(roomCode: String, guest: String, onResult: (Boolean) -> Unit) {
        val roomRef = firebase.roomsRef().child(roomCode)

        roomRef.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                onResult(false)
                return@addOnSuccessListener
            }

            val room = snapshot.getValue(Room::class.java)

            if (!room?.guestPlayer.isNullOrEmpty()) {
                onResult(false) // sala llena
                return@addOnSuccessListener
            }

            val updates = mapOf(
                "guestPlayer" to guest,
                "playerTurn" to room?.hostPlayer
            )

            roomRef.updateChildren(updates)
                .addOnSuccessListener { onResult(true) }
                .addOnFailureListener { onResult(false) }

        }.addOnFailureListener {
            onResult(false)
        }
    }

    fun listenRoom(roomCode: String) = callbackFlow {
        val ref = firebase.roomsRef().child(roomCode)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    trySend(null)
                    return
                }
                val room = snapshot.getValue(Room::class.java)
                trySend(room)
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        ref.addValueEventListener(listener)

        awaitClose {
            ref.removeEventListener(listener)
        }
    }
}
