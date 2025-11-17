package com.example.taller2.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


class ResultsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val score = 10

        setContent {
            ResultsScreen(
                score = score,
                onBackToStart = {

                    val intent = Intent(this, StartGameActivity::class.java)
                    startActivity(intent)
                    finish()
                },
                onGoToAccount = {

                    val intent = Intent(this, AccountActivity::class.java)
                    startActivity(intent)
                }
            )
        }
    }
}

@Composable
fun ResultsScreen(
    score: Int,
    onBackToStart: () -> Unit,
    onGoToAccount: () -> Unit
) {
    //temp
    val playersScores = listOf(
        "Player 1" to 10,
        "Player 2" to 7,
        "Player 3" to 15
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0B24))
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            Button(
                onClick = onGoToAccount,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7A3CFF)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Ir a Mi Cuenta", color = Color.White)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Game Results",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))


            playersScores.forEach { (playerName, playerScore) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B3C))
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = playerName,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                        )
                        Text(
                            text = "$playerScore",
                            color = Color(0xFFFFC107),
                            fontSize = 24.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))


            Button(
                onClick = onBackToStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7A3CFF))
            ) {
                Text("Back to Start", color = Color.White)
            }
        }
    }
}
