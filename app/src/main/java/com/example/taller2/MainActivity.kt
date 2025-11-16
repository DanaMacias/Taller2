package com.example.taller2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.example.taller2.ui.screens.LobbyScreen
import com.example.taller2.viewmodel.PlayerViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            val viewModel = PlayerViewModel()

            MaterialTheme {
                LobbyScreen(viewModel)
            }
        }
    }
}
