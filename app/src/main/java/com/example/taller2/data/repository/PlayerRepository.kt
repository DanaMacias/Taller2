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
        // 1. Usa push() para crear una nueva referencia de nodo única.
        val newPlayerRef = firebase.playersRef().push()

        // 2. Obtiene la clave única generada por push().
        val playerId = newPlayerRef.key ?: player.id

        // 3. Crea una copia del objeto Player con el ID de Firebase asignado.
        val playerWithId = player.copy(id = playerId)

        // 4. Guarda el objeto Player completo en el nodo recién creado.
        newPlayerRef.setValue(playerWithId)
    }

    fun updatePlayer(player: Player) {
        firebase.playersRef().child(player.id).setValue(player)
    }
    fun login(username: String, password: String) = callbackFlow<Player?> {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val user = snapshot.children
                    .mapNotNull { it.getValue(Player::class.java) }
                    .find { it.name == username && it.password == password }

                trySend(user)
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(null)
            }
        }

        firebase.playersRef().addListenerForSingleValueEvent(listener)

        awaitClose {}
    }


}
