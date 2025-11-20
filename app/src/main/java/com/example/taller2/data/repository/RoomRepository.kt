package com.example.taller2.data.repository

import android.util.Log
import com.example.taller2.data.firebase.FirebaseDataSource
import com.example.taller2.data.model.JoinResult
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
                generateUniqueRoomCode(onGenerated)
            } else {
                onGenerated(code)
            }
        }
    }

    fun createRoom(room: Room, onComplete: (Boolean) -> Unit) {
        val roomRef = firebase.roomsRef().child(room.id)
        roomRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                onComplete(false)
                return@addOnSuccessListener
            }
            roomRef.setValue(room).addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
        }.addOnFailureListener { onComplete(false) }
    }

    fun joinRoom(
        roomCode: String,
        userId: String,
        userName: String,
        onResult: (JoinResult) -> Unit
    ){
        val roomRef = firebase.roomsRef().child(roomCode)

        roomRef.get().addOnSuccessListener { snapshot ->
            val room = snapshot.getValue(Room::class.java)

            if (room == null) {
                onResult(JoinResult.RoomNotFound)
                return@addOnSuccessListener
            }

            if (!room.isActive) {
                Log.w("RoomRepo", "Sala inactiva, no se puede unir.")
                onResult(JoinResult.RoomInactive)
                return@addOnSuccessListener
            }

            if (room.players.size >= room.maxPlayers) {
                onResult(JoinResult.RoomFull)
                return@addOnSuccessListener
            }

            if (room.players.containsKey(userId)) {
                onResult(JoinResult.AlreadyJoined)
                return@addOnSuccessListener
            }

            val updatedPlayers = room.players.toMutableMap()
            updatedPlayers[userId] = userName

            val updatedStatus = room.playerStatus.toMutableMap()
            updatedStatus[userId] = false

            val updates = mapOf(
                "players" to updatedPlayers,
                "playerStatus" to updatedStatus
            )

            roomRef.updateChildren(updates)
                .addOnSuccessListener { onResult(JoinResult.Success) }
                .addOnFailureListener { onResult(JoinResult.Error) }

        }.addOnFailureListener {
            onResult(JoinResult.Error)
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
        awaitClose { ref.removeEventListener(listener) }
    }

    fun closeRoom(roomCode: String, onComplete: (Boolean) -> Unit) {
        val roomRef = firebase.roomsRef().child(roomCode)

        val updates = mapOf(
            "isActive" to false
        )

        Log.d("CloseRoom", "Cerrando sala $roomCode")

        roomRef.updateChildren(updates)
            .addOnSuccessListener {
                Log.d("CloseRoom", "Sala cerrada correctamente")
                onComplete(true)
            }
            .addOnFailureListener {
                Log.e("CloseRoom", "Error al cerrar sala: ${it.message}")
                onComplete(false)
            }
    }

    fun leaveRoom(roomCode: String, userId: String, onComplete: (Boolean) -> Unit) {
        val roomRef = firebase.roomsRef().child(roomCode)

        roomRef.get().addOnSuccessListener { snapshot ->
            val room = snapshot.getValue(Room::class.java) ?: return@addOnSuccessListener onComplete(false)

            val updatedPlayers = room.players.toMutableMap()
            updatedPlayers.remove(userId)

            val updatedStatus = room.playerStatus.toMutableMap()
            updatedStatus.remove(userId)

            val updates = mapOf(
                "players" to updatedPlayers,
                "playerStatus" to updatedStatus
            )

            roomRef.updateChildren(updates)
                .addOnSuccessListener { onComplete(true) }
                .addOnFailureListener { onComplete(false) }

        }.addOnFailureListener { onComplete(false) }
    }
}