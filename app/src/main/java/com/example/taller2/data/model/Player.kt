package com.example.taller2.data.model

import com.google.firebase.database.IgnoreExtraProperties
data class Player(
    val id: String = "",
    val name: String = "",
    val password: String = "",
    val email: String = "",
    val emoji: String = "",
    val isEliminated: Boolean = false,
    val turn: Boolean = false
)

