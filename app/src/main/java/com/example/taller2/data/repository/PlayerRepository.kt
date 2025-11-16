package com.example.taller2.data.repository

import com.example.taller2.data.firebase.FirebaseDataSource
import com.example.taller2.data.model.Player
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

class PlayerRepository(
    private val firebase: FirebaseDataSource = FirebaseDataSource()
) {

    fun getPlayers() = callbackFlow<List<Player>> {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val players = snapshot.children.mapNotNull {
                    it.getValue(Player::class.java)
                }
                trySend(players)
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        firebase.playersRef().addValueEventListener(listener)

        awaitClose {
            firebase.playersRef().removeEventListener(listener)
        }
    }

    fun addPlayer(player: Player) {
        firebase.playersRef().child(player.id).setValue(player)
    }

    fun updatePlayer(player: Player) {
        firebase.playersRef().child(player.id).setValue(player)
    }
}