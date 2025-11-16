package com.example.taller2.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.taller2.viewmodel.PlayerViewModel

@Composable
fun LobbyScreen(viewModel: PlayerViewModel) {

    val players by viewModel.players.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Jugadores en la sala", style = MaterialTheme.typography.headlineSmall)

        LazyColumn {
            items(players.size) { i ->
                Text("${players[i].name} - ${players[i].emoji}")
            }
        }
    }
}