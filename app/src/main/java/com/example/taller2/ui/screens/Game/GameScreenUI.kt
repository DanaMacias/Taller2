package com.example.taller2.ui.screens.Game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GameScreenUI(
    viewModel: GameViewModel
) {
    val state = viewModel.gameState.collectAsState().value

    var messageText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color(0xFFF0F0F0)),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "Tiempo: ${state?.timerSeconds ?: 60}s",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                Button(
                    onClick = { viewModel.pauseGame() },
                    enabled = state?.isPaused == false
                ) { Text("Pausa") }

                Button(
                    onClick = { viewModel.resumeGame() },
                    enabled = state?.isPaused == true
                ) { Text("Reanudar") }

                if (state?.isPaused == true) {
                    Button(onClick = { viewModel.resumeGame() }) {
                        Text("Continuar")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Turno: ${state?.currentPlayer ?: ""}",
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = Color(0xFF4A148C)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Jugadores:",
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        state?.players?.forEach { name ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(Color.White, shape = RoundedCornerShape(8.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "😊", fontSize = 26.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = name, fontSize = 18.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.White, RoundedCornerShape(10.dp))
                .padding(8.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
            ) {
                items(state?.messages ?: emptyList()) { msg ->
                    ChatBubble(message = msg.text, isSelf = false)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Escribe un mensaje...") }
                )
                Spacer(modifier = Modifier.width(6.dp))
                Button(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            viewModel.sendChatMessage(messageText)
                            messageText = ""
                        }
                    }
                ) {
                    Text("Enviar")
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: String, isSelf: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isSelf) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = if (isSelf) Color(0xFFD1C4E9) else Color(0xFFBBDEFB),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(10.dp)
        ) {
            Text(message)
        }
    }
    Spacer(modifier = Modifier.height(4.dp))
}
