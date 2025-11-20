package com.example.taller2.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.taller2.viewmodel.RoomViewModel

@Composable
fun LobbyScreen(
    viewModel: RoomViewModel,
    onStartGame: () -> Unit = {},
    onExitLobby: () -> Unit
) {
    val roomState by viewModel.room.collectAsState()

    val context = LocalContext.current
    val shared = remember { context.getSharedPreferences("user_session", Context.MODE_PRIVATE) }
    val myUserId = shared.getString("player_id", "") ?: ""

    val room = roomState ?: return
    val amIHost = room.hostId == myUserId
    val canStart = room.players.size >= 2
    var isExiting by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Código de Sala: ${room.id}",
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xFF7A3CFF),
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Jugadores (${room.players.size}/${room.maxPlayers})",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.height(16.dp))

        //Lista de Jugadores
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(room.players.toList()) { (playerId, playerName) ->
                val isMe = playerId == myUserId
                val isHost = playerId == room.hostId
                PlayerCard(name = playerName, isMe = isMe, isHost = isHost)
            }
        }

        Spacer(Modifier.height(20.dp))

        val amIHost = room.hostId == myUserId
        val canStart = room.players.size >= 2

        if (amIHost) {
            Button(
                onClick = onStartGame,
                enabled = canStart,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canStart) Color(0xFF7A3CFF) else Color.Gray
                )
            ) {
                Text("Iniciar Partida", color = Color.White)
            }
            if (!canStart) {
                Text("Esperando más jugadores...", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        } else {
            Text(
                "Esperando a que el anfitrión inicie la partida...",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
        Spacer(Modifier.height(10.dp))

        Button(
            onClick = {
                isExiting = true
                if (amIHost) {
                    viewModel.closeRoom(room.id) { success ->
                        if (success) {
                            viewModel.clearRoomState()
                            onExitLobby()
                        } else {
                            isExiting = false
                        }
                    }
                } else {
                    viewModel.leaveRoom(room.id, myUserId) { success ->
                        if (success) {
                            viewModel.clearRoomState()
                            onExitLobby()
                        } else {
                            isExiting = false
                        }
                    }
                }
            },
            enabled = !isExiting,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red.copy(alpha = 0.8f)
            )
        ) {
            if (isExiting) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
                Text(if (amIHost) "Cerrar y Desactivar Sala" else "Abandonar Sala", color = Color.White)
            }
        }
    }
}

@Composable
fun PlayerCard(name: String, isMe: Boolean, isHost: Boolean) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isMe) Color(0xFF2D2B4C) else Color(0xFF1E1B3C)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = if (isMe) "$name (Tú)" else name,
                    color = Color.White,
                    fontWeight = if (isMe) FontWeight.Bold else FontWeight.Normal
                )
            }

            if (isHost) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Host",
                    tint = Color.Yellow
                )
            }
        }
    }
}