package com.example.taller2.ui.screens.Account

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taller2.GameActivity

class AccountActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val shared = getSharedPreferences("user_session", MODE_PRIVATE)

        val userId = shared.getString("player_id", "") ?: ""

        setContent {
            AccountScreen(
                userId = userId,
                onBackToStart = {
                    val intent = Intent(this, GameActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            )
        }
    }
}


@Composable
fun AccountScreen(
    onBackToStart: () -> Unit,
    userId: String,
    viewModel: AccountViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val user = viewModel.player.collectAsState().value

    LaunchedEffect(Unit) {
        viewModel.loadUser(userId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0B24))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Mi Cuenta",
            color = Color.White,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (user != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B3C))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Nombre: ${user.name}", color = Color.White, fontSize = 18.sp)
                    Text(text = "Email: ${user.email}", color = Color.White, fontSize = 18.sp)
                }
            }
        } else {
            CircularProgressIndicator(color = Color.White)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onBackToStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7A3CFF))
        ) {
            Text("Volver", color = Color.White)
        }
    }
}

