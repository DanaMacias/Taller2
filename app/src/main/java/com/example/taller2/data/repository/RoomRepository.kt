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
        roomRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val room = currentData.getValue(Room::class.java)
                if (room == null) {
                    // La sala no existe, se abortará y onComplete lo manejará.
                    return Transaction.abort()
                }

                if (!room.isActive) {
                    return Transaction.abort() // Aborta si la sala está inactiva.
                }

                if (room.players.size >= room.maxPlayers) {
                    return Transaction.abort() // Aborta si la sala está llena.
                }

                if (room.players.containsKey(userId)) {
                    return Transaction.abort() // Aborta si el jugador ya está dentro.
                }

                val updatedPlayers = room.players.toMutableMap()
                updatedPlayers[userId] = userName

                val updatedStatus = room.playerStatus.toMutableMap()
                updatedStatus[userId] = false

                currentData.child("players").value = updatedPlayers
                currentData.child("playerStatus").value = updatedStatus

                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                if (error != null) {
                    onResult(JoinResult.Error)
                } else if (!committed) {
                    // La transacción fue abortada, determinamos la razón exacta.
                    val room = currentData?.getValue(Room::class.java)
                    if (room == null) {
                         onResult(JoinResult.RoomNotFound)
                    } else if (!room.isActive) {
                         onResult(JoinResult.RoomInactive)
                    } else if (room.players.size >= room.maxPlayers) {
                        onResult(JoinResult.RoomFull)
                    } else if (room.players.containsKey(userId)) {
                        onResult(JoinResult.AlreadyJoined)
                    } else {
                        onResult(JoinResult.Error) // Fallo genérico
                    }
                } else {
                    onResult(JoinResult.Success)
                }
            }
        })
    }


    fun listenRoom(roomCode: String) = callbackFlow {
        val ref = firebase.roomsRef().child(roomCode)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    trySend(null)
                    close()
                    return
                }
                val room = snapshot.getValue(Room::class.java)
                trySend(room)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun deleteRoom(roomCode: String, onComplete: (Boolean) -> Unit) {
        firebase.roomsRef().child(roomCode).removeValue()
            .addOnSuccessListener {
                Log.d("DeleteRoom", "Sala eliminada correctamente")
                onComplete(true)
            }
            .addOnFailureListener {
                Log.e("DeleteRoom", "Error al eliminar la sala: ${it.message}")
                onComplete(false)
            }
    }

    fun leaveRoom(roomCode: String, userId: String, onComplete: (Boolean) -> Unit) {
        val roomRef = firebase.roomsRef().child(roomCode)

        roomRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val room = currentData.getValue(Room::class.java)
                    ?: return Transaction.success(currentData) // La sala no existe, no hay nada que hacer.

                if (!room.players.containsKey(userId)) {
                    return Transaction.success(currentData) // El jugador no está en la sala.
                }

                val updatedPlayers = room.players.toMutableMap()
                updatedPlayers.remove(userId)

                // Si el jugador que se va es el anfitrión, eliminamos la sala.
                if (room.hostId == userId) {
                    currentData.value = null
                    return Transaction.success(currentData)
                }

                val updatedStatus = room.playerStatus.toMutableMap()
                updatedStatus.remove(userId)

                // Si la sala queda vacía, también la eliminamos.
                if (updatedPlayers.isEmpty()) {
                    currentData.value = null
                } else {
                    // Actualizamos solo los mapas necesarios.
                    currentData.child("players").value = updatedPlayers
                    currentData.child("playerStatus").value = updatedStatus
                }

                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                // `onComplete` se llama siempre, tanto en éxito como en fallo.
                // Esto garantiza que la animación de carga no se quede congelada.
                onComplete(error == null && committed)
            }
        })
    }

    fun startGame(roomId: String, onComplete: (Boolean) -> Unit) {
        firebase.roomsRef().child(roomId).child("gameStarted")
            .setValue(true)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun updateRoomField(
        roomId: String,
        field: String,
        value: Any,
        callback: (Boolean) -> Unit
    ) {
        firebase.roomsRef().child(roomId)
            .child(field)
            .setValue(value)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }
}