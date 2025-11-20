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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taller2.data.model.ChatMessage
// Asegúrate de importar tu ViewModel y GameState correctos aquí

@Composable
fun GameScreenUI(
    roomId: String,
    viewModel: GameViewModel,
    myUserId: String,
    myUserName: String,
    onExitGame: () -> Unit
) {
    // --- 1. Estados del ViewModel ---
    val gameState by viewModel.gameState.collectAsState()
    val room by viewModel.room.collectAsState()
    val chatMessages by viewModel.chat.collectAsState()
    val timeRemaining by viewModel.timeRemaining.collectAsState()

    // Iniciar escucha al entrar
    LaunchedEffect(roomId) {
        viewModel.startListening(roomId)
    }

    // Pantalla de carga
    if (gameState == null || room == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF6200EE))
            Spacer(Modifier.height(8.dp))
            Text("Sincronizando partida...", color = Color.White, modifier = Modifier.padding(top = 48.dp))
        }
        return
    }

    // --- 2. Variables Lógicas ---
    val playersOrder = gameState!!.playersOrder
    val currentTurnIndex = gameState!!.currentTurnIndex
    val currentPlayerId = playersOrder.getOrNull(currentTurnIndex) ?: ""
    val isMyTurn = currentPlayerId == myUserId
    val gameEnded = !gameState!!.started && gameState!!.guesses.isNotEmpty() // O usa gameState!!.gameEnded si ya lo tienes

    // Lógica de Eliminación
    val amIEliminated = gameState!!.eliminatedPlayers.contains(myUserId)

    // Colores Tema
    val bgDark = Color(0xFF121212)
    val cardColor = Color(0xFF1E1E1E)
    val accentColor = Color(0xFFBB86FC)
    val eliminatedColor = Color.Red

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgDark)
            .padding(16.dp)
    ) {

        // --- HEADER: Info Sala y Timer ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Sala: $roomId", color = Color.Gray, fontSize = 12.sp)
                if (gameEnded) {
                    Text("¡PARTIDA FINALIZADA!", color = Color.Green, fontWeight = FontWeight.Bold)
                } else if (amIEliminated) {
                    Text("ESPECTADOR (ELIMINADO)", color = eliminatedColor, fontWeight = FontWeight.Bold)
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

        // --- LISTA DE JUGADORES ---
        Text("Jugadores:", color = Color.Gray, fontSize = 14.sp)
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp), // Un poco más alto para acomodar estados
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(playersOrder) { pid ->
                val name = room?.players?.get(pid) ?: "?"
                // Si soy yo -> ❓ (o emoji asignado), Si es otro -> Emoji asignado
                val assignedEmoji = gameState!!.assignedEmojis[pid] ?: ""
                val displayEmoji = if (pid == myUserId) "❓" else assignedEmoji

                val hasGuessed = gameState!!.guesses.containsKey(pid)
                val isPlayerEliminated = gameState!!.eliminatedPlayers.contains(pid)

                PlayerGameCard(
                    name = name,
                    emoji = if (hasGuessed) "✅" else displayEmoji,
                    isActive = (pid == currentPlayerId),
                    isMe = (pid == myUserId),
                    isEliminated = isPlayerEliminated
                )
            }
        }

        Divider(color = Color.DarkGray, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

        // --- ÁREA DE CHAT ---
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

        // --- FOOTER: Controles (Eliminado vs Jugando vs Fin) ---
        if (gameEnded) {
            Button(
                onClick = onExitGame,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Salir de la partida")
            }
        } else if (amIEliminated) {
            // --- VISTA DE ELIMINADO ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Red.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .border(1.dp, Color.Red, RoundedCornerShape(8.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.Red)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "❌ ESTÁS ELIMINADO",
                        color = Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            // --- JUGADOR ACTIVO ---
            Column {
                // 1. Selector de Emoji (Solo si es mi turno)
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

                // 2. Input Chat
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
                            unfocusedTextColor = Color.White,
                            focusedIndicatorColor = accentColor,
                            unfocusedIndicatorColor = Color.Gray
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
}

@Composable
fun PlayerGameCard(
    name: String,
    emoji: String,
    isActive: Boolean,
    isMe: Boolean,
    isEliminated: Boolean
) {
    val borderColor = when {
        isEliminated -> Color.Red
        isActive -> Color.Yellow
        else -> Color.Transparent
    }

    val backgroundColor = when {
        isEliminated -> Color(0xFF2B1111) // Rojo oscuro muy suave
        isMe -> Color(0xFF3A3A3A)
        else -> Color(0xFF222222)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp) // Un poco más ancho
            .alpha(if (isEliminated) 0.6f else 1f) // Transparencia si está eliminado
            .border(
                width = if (isActive || isEliminated) 2.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        if (isEliminated) {
            // Icono de eliminado en lugar del emoji
            Icon(
                Icons.Default.Close,
                contentDescription = "Eliminado",
                tint = Color.Red,
                modifier = Modifier.size(30.dp)
            )
        } else {
            Text(text = emoji, fontSize = 30.sp)
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = if (isMe) "Yo" else name,
            color = if (isEliminated) Color.Red else Color.White,
            fontSize = 11.sp,
            maxLines = 1,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
        )

        if (isEliminated) {
            Text("Eliminado", color = Color.Red, fontSize = 9.sp)
        }
    }
}

@Composable
fun ChatBubble(msg: ChatMessage, isMe: Boolean) {
    val isSystem = msg.senderId == "SYS"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = if (isSystem) Arrangement.Center else (if (isMe) Arrangement.End else Arrangement.Start)
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(
                    when {
                        isSystem -> Color(0xFFFFC107).copy(alpha = 0.2f) // Amarillo transparente para sistema
                        isMe -> Color(0xFF6200EE)
                        else -> Color(0xFF333333)
                    }
                )
                .padding(8.dp)
                .widthIn(max = 260.dp),
            horizontalAlignment = if(isSystem) Alignment.CenterHorizontally else Alignment.Start
        ) {
            if (!isMe && !isSystem) {
                Text(msg.senderName, color = Color(0xFFBB86FC), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Text(
                text = msg.text,
                color = if (isSystem) Color(0xFFFFD54F) else Color.White,
                fontSize = 14.sp,
                fontWeight = if (isSystem) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}