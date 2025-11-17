package com.example.taller2.ui.screens.room

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taller2.viewmodel.RoomViewModel

@Composable
fun RoomScreen(
    roomViewModel: RoomViewModel = viewModel()
) {
    var screenMode by remember { mutableStateOf("menu") }
    var playerName by remember { mutableStateOf("") }
    var roomCode by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    val roomState by roomViewModel.room.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0B24))
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        when (screenMode) {

            "menu" -> {
                Text(
                    "¿Qué deseas hacer?",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = { screenMode = "create" },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7A3CFF))
                ) {
                    Text("Crear Sala", color = Color.White)
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = { screenMode = "join" },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7A3CFF))
                ) {
                    Text("Unirme a Sala", color = Color.White)
                }
            }

            "create" -> {
                Text("Crear Sala", color = Color.White, style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(20.dp))

                OutlinedTextField(
                    value = playerName,
                    onValueChange = { playerName = it },
                    label = { Text("Tu nombre", color = Color.White) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White,
                        focusedContainerColor = Color(0xFF1E1B3C),
                        unfocusedContainerColor = Color(0xFF1E1B3C),
                        focusedBorderColor = Color(0xFF7A3CFF),
                        unfocusedBorderColor = Color.White
                    )
                )

                Spacer(Modifier.height(20.dp))

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7A3CFF)),
                    onClick = {
                        if (playerName.isBlank()) {
                            message = "Por favor escribe tu nombre."
                            return@Button
                        }

                        roomViewModel.generateRoomCode { generated ->
                            roomCode = generated

                            roomViewModel.createRoom(generated, playerName) { success ->
                                message = if (success) {
                                    roomViewModel.startListening(generated)
                                    "Sala creada correctamente"
                                } else "Error: ya existe una sala con ese código."
                            }
                        }
                    }
                ) {
                    Text("Generar código y crear sala", color = Color.White)
                }

                Spacer(Modifier.height(20.dp))

                if (roomCode.isNotBlank()) {
                    Text("Código de sala (compártelo):", color = Color.White)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        roomCode,
                        color = Color(0xFF7A3CFF),
                        style = MaterialTheme.typography.headlineLarge
                    )
                }

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = { screenMode = "menu" },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7A3CFF))
                ) {
                    Text("Volver", color = Color.White)
                }
            }

            "join" -> {
                Text("Unirme a Sala", color = Color.White, style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(20.dp))

                OutlinedTextField(
                    value = playerName,
                    onValueChange = { playerName = it },
                    label = { Text("Tu nombre", color = Color.White) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White,
                        focusedContainerColor = Color(0xFF1E1B3C),
                        unfocusedContainerColor = Color(0xFF1E1B3C),
                        focusedBorderColor = Color(0xFF7A3CFF),
                        unfocusedBorderColor = Color.White
                    )
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = roomCode,
                    onValueChange = {
                        if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                            roomCode = it
                        }
                    },
                    label = { Text("Código de sala", color = Color.White) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White,
                        focusedContainerColor = Color(0xFF1E1B3C),
                        unfocusedContainerColor = Color(0xFF1E1B3C),
                        focusedBorderColor = Color(0xFF7A3CFF),
                        unfocusedBorderColor = Color.White
                    )
                )

                Spacer(Modifier.height(20.dp))

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7A3CFF)),
                    onClick = {
                        if (playerName.isBlank() || roomCode.length != 4) {
                            message = "Completa tu nombre y un código válido."
                            return@Button
                        }

                        roomViewModel.joinRoom(roomCode, playerName) { success ->
                            message = if (success) {
                                roomViewModel.startListening(roomCode)
                                "Unido con éxito"
                            } else "La sala no existe o está llena."
                        }
                    }
                ) {
                    Text("Unirme", color = Color.White)
                }

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = { screenMode = "menu" },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7A3CFF))
                ) {
                    Text("Volver", color = Color.White)
                }
            }
        }

        if (message.isNotBlank()) {
            Spacer(Modifier.height(20.dp))
            Text(text = message, color = Color(0xFF7A3CFF))
        }

        roomState?.let { room ->
            Spacer(Modifier.height(30.dp))
            Divider(color = Color.White.copy(alpha = 0.3f))
            Spacer(Modifier.height(20.dp))

            Text("Sala activa:", color = Color.White)
            Text("Código: ${room.id}", color = Color.White)
            Text("Host: ${room.hostPlayer}", color = Color.White)

            Spacer(Modifier.height(12.dp))

            Text("Jugadores:", color = Color.White)
            room.players.forEach { playerName ->
                Text("- $playerName", color = Color.White)
            }

            Spacer(Modifier.height(20.dp))

            val canStart = room.players.size >= 2 && room.players.size <= 4

            Button(
                onClick = { /* TODO: iniciar partida */ },
                enabled = canStart,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canStart) Color(0xFF7A3CFF) else Color.Gray
                )
            ) {
                Text("Iniciar partida", color = Color.White)
            }
        }


    }
}