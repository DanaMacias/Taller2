package com.example.taller2.ui.screens.Game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel

class GameScreen : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val roomCode = intent.getStringExtra("roomCode") ?: ""

        setContent {
            val gameViewModel: GameViewModel = viewModel()

            // Iniciar el juego para esta sala
            gameViewModel.startListening(roomCode)

            GameScreenUI(
                viewModel = gameViewModel
            )
        }
    }
}
