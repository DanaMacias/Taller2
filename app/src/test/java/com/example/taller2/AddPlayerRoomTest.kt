package com.example.taller2

import com.example.taller2.data.model.Room
import com.example.taller2.utils.addPlayerToRoomLocal
import org.junit.Assert.*
import org.junit.Test

class AddPlayerRoomTest {

    @Test
    fun testAddNewPlayer() {
        val initialRoom = Room(
            id = "1234",
            hostPlayer = "Alex",
            players = listOf("Alex")
        )

        val updatedRoom = addPlayerToRoomLocal(initialRoom, "Javi")

        assertNotNull(updatedRoom)
        assertEquals(2, updatedRoom!!.players.size)
        assertTrue(updatedRoom.players.contains("Javi"))
    }

    @Test
    fun testAddExistingPlayer() {
        val room = Room(
            id = "1234",
            hostPlayer = "Alex",
            players = listOf("Alex", "Javi")
        )

        val result = addPlayerToRoomLocal(room, "Alex")

        assertEquals(2, result!!.players.size)
    }

    @Test
    fun testRoomFull() {
        val room = Room(
            id = "1234",
            hostPlayer = "Alex",
            players = listOf("Alex", "Javi", "Pedro", "Pedro" )
        )

        val result = addPlayerToRoomLocal(room, "Sofi")

        assertNull(result)
    }
}


