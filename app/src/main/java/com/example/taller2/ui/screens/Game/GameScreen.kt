package com.example.taller2.ui.screens.Game

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel

class GameScreen : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val roomId = intent.getStringExtra("roomCode") ?: ""

        val shared = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val myUserId = shared.getString("player_id", "") ?: ""
        val myUserName = shared.getString("player_name", "Jugador") ?: "Jugador"

        setContent {

            val gameViewModel: GameViewModel = viewModel()

            LaunchedEffect(roomId) {
                if (roomId.isNotEmpty()) {
                    gameViewModel.startListening(roomId)
                }
            }

            GameScreenUI(
                roomId = roomId,
                viewModel = gameViewModel,
                myUserId = myUserId,
                myUserName = myUserName,
                onExitGame = {
                    finish()
                }
            )
        }
    }
}