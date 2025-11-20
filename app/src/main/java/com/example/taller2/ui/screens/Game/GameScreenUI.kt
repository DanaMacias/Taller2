package com.example.taller2.ui.screens.Game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GameScreenUI(
    roomId: String,
    viewModel: GameViewModel,
    myUserId: String,
    myUserName: String,
    onExitGame: () -> Unit
) {
    // Estados recolectados del ViewModel
    val gameState by viewModel.gameState.collectAsState()
    val room by viewModel.room.collectAsState()
    val chatMessages by viewModel.chat.collectAsState()
    val timeRemaining by viewModel.timeRemaining.collectAsState() // ✅ Timer reactivo

    // Iniciar escucha al entrar
    LaunchedEffect(roomId) {
        viewModel.startListening(roomId)
    }

    // Pantalla de carga si no hay estado
    if (gameState == null || room == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF6200EE))
            Spacer(Modifier.height(8.dp))
            Text("Sincronizando partida...", color = Color.White)
        }
        return
    }

    // Variables auxiliares
    val playersOrder = gameState!!.playersOrder
    val currentTurnIndex = gameState!!.currentTurnIndex
    val currentPlayerId = playersOrder.getOrNull(currentTurnIndex) ?: ""
    val isMyTurn = currentPlayerId == myUserId
    val gameEnded = !gameState!!.started && gameState!!.guesses.isNotEmpty()

    // Colores Tema
    val bgDark = Color(0xFF121212)
    val cardColor = Color(0xFF1E1E1E)
    val accentColor = Color(0xFFBB86FC)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgDark)
            .padding(16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Sala: $roomId", color = Color.Gray, fontSize = 12.sp)
                if (gameEnded) {
                    Text("¡PARTIDA FINALIZADA!", color = Color.Green, fontWeight = FontWeight.Bold)
                } else {
                    val currentName = room?.players?.get(currentPlayerId) ?: "..."
                    Text(
                        text = if (isMyTurn) "¡TU TURNO!" else "Turno de: $currentName",
                        color = if (isMyTurn) Color.Yellow else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }

            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = timeRemaining / 60f,
                    color = if (timeRemaining < 10) Color.Red else accentColor,
                    modifier = Modifier.size(45.dp)
                )
                Text(
                    text = "$timeRemaining",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Text("Jugadores (Pistas):", color = Color.Gray, fontSize = 14.sp)
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(playersOrder) { pid ->
                val name = room?.players?.get(pid) ?: "?"
                // LÓGICA DE ORO: Si soy yo -> ❓, Si es otro -> Emoji Real
                val assignedEmoji = gameState!!.assignedEmojis[pid] ?: ""
                val displayEmoji = if (pid == myUserId) "❓" else assignedEmoji
                val hasGuessed = gameState!!.guesses.containsKey(pid)

                PlayerGameCard(
                    name = name,
                    emoji = if (hasGuessed) "✅" else displayEmoji,
                    isActive = (pid == currentPlayerId),
                    isMe = (pid == myUserId)
                )
            }
        }

        Divider(color = Color.DarkGray, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .background(cardColor, RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            val listState = rememberLazyListState()
            // Auto-scroll al recibir mensaje
            LaunchedEffect(chatMessages.size) {
                if (chatMessages.isNotEmpty()) listState.animateScrollToItem(chatMessages.size - 1)
            }

            LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                items(chatMessages) { msg ->
                    ChatBubble(msg, isMe = msg.senderId == myUserId)
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        if (gameEnded) {
            Button(
                onClick = onExitGame,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Salir de la partida")
            }
        } else {
            // Si es MI TURNO, muestro los emojis para elegir
            if (isMyTurn) {
                Text(
                    "SELECCIONA TU EMOJI:",
                    color = Color.Yellow,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    // Usamos la lista predefinida en el ViewModel o las opciones posibles
                    items(viewModel.availableEmojis) { emojiOption ->
                        Button(
                            onClick = { viewModel.submitGuess(roomId, myUserId, emojiOption) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333)),
                            shape = CircleShape,
                            modifier = Modifier.size(50.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(emojiOption, fontSize = 22.sp)
                        }
                    }
                }
            }

            // Input Chat
            Row(verticalAlignment = Alignment.CenterVertically) {
                var textState by remember { mutableStateOf("") }
                TextField(
                    value = textState,
                    onValueChange = { textState = it },
                    placeholder = { Text("Escribe una pista...", color = Color.Gray) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {
                    if (textState.isNotBlank()) {
                        viewModel.sendChatMessage(roomId, myUserId, myUserName, textState)
                        textState = ""
                    }
                }) {
                    Icon(Icons.Default.Send, contentDescription = "Enviar", tint = accentColor)
                }
            }
        }
    }
}

@Composable
fun PlayerGameCard(name: String, emoji: String, isActive: Boolean, isMe: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(75.dp)
            .border(
                width = if (isActive) 2.dp else 0.dp,
                color = if (isActive) Color.Yellow else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .background(if (isMe) Color(0xFF3A3A3A) else Color(0xFF222222), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Text(text = emoji, fontSize = 30.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (isMe) "Yo" else name,
            color = Color.White,
            fontSize = 11.sp,
            maxLines = 1,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun ChatBubble(msg: com.example.taller2.data.model.ChatMessage, isMe: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(if (isMe) Color(0xFF6200EE) else Color(0xFF333333))
                .padding(8.dp)
                .widthIn(max = 260.dp)
        ) {
            if (!isMe) {
                Text(msg.senderName, color = Color(0xFFBB86FC), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Text(msg.text, color = Color.White, fontSize = 14.sp)
        }
    }
}