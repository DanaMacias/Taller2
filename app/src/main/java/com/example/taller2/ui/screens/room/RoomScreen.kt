package com.example.taller2.ui.screens.room

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
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
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        when (screenMode) {

            // ----------------------------------------------------------
            // 🔵 MENÚ PRINCIPAL
            // ----------------------------------------------------------
            "menu" -> {
                Text(
                    "¿Qué deseas hacer?",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = { screenMode = "create" },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Crear Sala")
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = { screenMode = "join" },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Unirme a Sala")
                }
            }


            // ----------------------------------------------------------
            // 🔶 CREAR SALA
            // ----------------------------------------------------------
            "create" -> {
                Text("Crear Sala", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(20.dp))

                OutlinedTextField(
                    value = playerName,
                    onValueChange = { playerName = it },
                    label = { Text("Tu nombre") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(20.dp))

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        if (playerName.isBlank()) {
                            message = "Por favor escribe tu nombre."
                            return@Button
                        }

                        // Generar código y crear sala
                        roomViewModel.generateRoomCode { generated ->

                            roomCode = generated

                            roomViewModel.createRoom(generated, playerName) { success ->
                                if (success) {
                                    message = "Sala creada correctamente"
                                    roomViewModel.startListening(generated)
                                } else {
                                    message = "Error: ya existe una sala con ese código."
                                }
                            }
                        }
                    }
                ) {
                    Text("Generar código y crear sala")
                }

                Spacer(Modifier.height(20.dp))

                if (roomCode.isNotBlank()) {
                    Text("Código de sala (compártelo):", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        roomCode,
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = { screenMode = "menu" },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Volver")
                }
            }


            // ----------------------------------------------------------
            // 🔷 UNIRME A SALA
            // ----------------------------------------------------------
            "join" -> {
                Text("Unirme a Sala", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(20.dp))

                OutlinedTextField(
                    value = playerName,
                    onValueChange = { playerName = it },
                    label = { Text("Tu nombre") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = roomCode,
                    onValueChange = {
                        if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                            roomCode = it
                        }
                    },
                    label = { Text("Código de sala") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(20.dp))

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        if (playerName.isBlank() || roomCode.length != 4) {
                            message = "Completa tu nombre y un código válido."
                            return@Button
                        }

                        roomViewModel.joinRoom(roomCode, playerName) { success ->
                            if (success) {
                                message = "Unido con éxito"
                                roomViewModel.startListening(roomCode)
                            } else {
                                message = "La sala no existe o está llena."
                            }
                        }
                    }
                ) {
                    Text("Unirme")
                }

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = { screenMode = "menu" },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Volver")
                }
            }
        }


        // ----------------------------------------------------------
        // MENSAJES
        // ----------------------------------------------------------
        if (message.isNotBlank()) {
            Spacer(Modifier.height(20.dp))
            Text(
                text = message,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // ----------------------------------------------------------
        // ESTADO DE LA SALA EN TIEMPO REAL
        // ----------------------------------------------------------
        roomState?.let { room ->
            Spacer(Modifier.height(30.dp))
            Divider()
            Spacer(Modifier.height(20.dp))
            Text("Sala activa:", style = MaterialTheme.typography.titleMedium)
            Text("Código: ${room.id}")
            Text("Host: ${room.hostPlayer}")
            Text("Invitado: ${room.guestPlayer ?: "Esperando..."}")
            Text("Turno: ${room.playerTurn ?: "N/A"}")
        }
    }
}
