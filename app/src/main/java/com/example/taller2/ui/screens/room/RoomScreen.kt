package com.example.taller2.ui.screens.room

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taller2.data.model.JoinResult
import com.example.taller2.ui.screens.Game.GameScreen
import com.example.taller2.ui.screens.LobbyScreen
import com.example.taller2.viewmodel.RoomViewModel

@Composable
fun RoomScreen(
    roomViewModel: RoomViewModel = viewModel()
) {
    val context = LocalContext.current

    val shared = remember { context.getSharedPreferences("user_session", Context.MODE_PRIVATE) }

    val sessionUserId = shared.getString("player_id", "") ?: ""
    val sessionUserName = shared.getString("player_name", "Jugador") ?: "Jugador"

    if (sessionUserId.isBlank()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Error: No se encontró sesión activa.", color = Color.Red)
        }
        return
    }

    var screenMode by remember { mutableStateOf("menu") }
    var roomCodeInput by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    val roomState by roomViewModel.room.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0B24))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if (roomState != null) {
                LobbyScreen(
                    viewModel = roomViewModel,
                    onStartGame = {
                        val intent = Intent(context, GameScreen::class.java)
                        intent.putExtra("roomCode", roomState!!.id)
                        context.startActivity(intent)
                    },
                    onExitLobby = {
                        screenMode = "menu"
                        message = "Has salido de la sala."
                    }
                )
        } else {
            Text("Hola, $sessionUserName", color = Color.Gray)
            Spacer(Modifier.height(16.dp))

            when (screenMode) {
                "menu" -> {
                    Button(onClick = { screenMode = "create"
                        message = "" }) { Text("Crear Sala") }
                    Spacer(Modifier.height(10.dp))
                    Button(onClick = { screenMode = "join"
                        message = ""}) { Text("Unirme a Sala") }
                }

                "create" -> {
                    Text("Crear Sala", color = Color.White, style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = {
                            roomViewModel.generateRoomCode { code ->
                                roomViewModel.createRoom(code, sessionUserId, sessionUserName) { success ->
                                    if (success) {
                                        roomViewModel.startListening(code)
                                        message = "Sala creada"
                                    } else {
                                        message = "Error al crear"
                                    }
                                }
                            }
                        }
                    ) {
                        Text("Generar y Crear")
                    }
                    Button(onClick = { screenMode = "menu"
                        message = ""}) { Text("Volver") }
                }

                "join" -> {
                    OutlinedTextField(
                        value = roomCodeInput,
                        onValueChange = { roomCodeInput = it },
                        label = { Text("Código de la sala") }
                    )

                    Button(
                        onClick = {
                            roomViewModel.joinRoom(roomCodeInput, sessionUserId, sessionUserName) { result ->

                                when (result) {
                                    is JoinResult.Success -> {
                                        roomViewModel.startListening(roomCodeInput)
                                        message = "Te uniste con éxito"
                                    }
                                    is JoinResult.RoomNotFound -> message = "❌ La sala no existe"
                                    is JoinResult.RoomInactive -> message = "⚠ La sala está cerrada"
                                    is JoinResult.RoomFull -> message = "🚫 La sala está llena"
                                    is JoinResult.AlreadyJoined -> {
                                        roomViewModel.startListening(roomCodeInput)
                                        message = "✔ Ya estabas en esta sala"
                                    }
                                    is JoinResult.Error -> message = "⚠ Ocurrió un error inesperado"
                                }
                            }
                        },
                        enabled = roomCodeInput.isNotBlank()
                    ) {
                        Text("Unirme")
                    }
                    Button(onClick = { screenMode = "menu"
                        message = ""}) { Text("Volver") }
                }

            }
        }

        if (message.isNotBlank()) Text(message, color = Color.Magenta)
    }
}