

package com.example.taller2.utils

import com.example.taller2.data.model.Room

fun addPlayerToRoomLocal(room: Room, player: String): Room? {
    if (room.players.contains(player)) return room
    if (room.players.size >= 4) return null

    val updatedPlayers = room.players //+ player

    return room.copy(players = updatedPlayers)
}
