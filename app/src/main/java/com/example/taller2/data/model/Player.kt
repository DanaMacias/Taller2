package com.example.taller2.data.model

data class Player(
    val id: String = "",
    val name: String = "",
    val emoji: String = "",
    val isEliminated: Boolean = false,
    val turn: Boolean = false
)