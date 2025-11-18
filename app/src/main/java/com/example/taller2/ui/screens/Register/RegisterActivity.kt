package com.example.taller2.ui.screens.Register



import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.taller2.GameActivity
import com.example.taller2.ui.screens.login.LoginActivity


class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            RegisterScreen(
                onRegister = {
                    val intent = Intent(this, GameActivity::class.java)
                    startActivity(intent)
                },
                onBackToLogin = {
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)



                }
            )
        }
    }
}
@Composable
fun RegisterScreen(
    onRegister: () -> Unit = {},
    onBackToLogin: () -> Unit = {}
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0B24))
            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Create Account",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            TextField(
                value = fullName,
                onValueChange = { fullName = it },
                placeholder = { Text("Full Name", color = Color.Gray) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0x33FFFFFF),
                    unfocusedContainerColor = Color(0x22FFFFFF),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("Email", color = Color.Gray) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0x33FFFFFF),
                    unfocusedContainerColor = Color(0x22FFFFFF),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Password", color = Color.Gray) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0x33FFFFFF),
                    unfocusedContainerColor = Color(0x22FFFFFF),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = { Text("Confirm Password", color = Color.Gray) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0x33FFFFFF),
                    unfocusedContainerColor = Color(0x22FFFFFF),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onRegister,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7A3CFF)
                )
            ) {
                Text("Sign Up", color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onBackToLogin,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                border = BorderStroke(2.dp, Color(0xFF49A8FF))
            ) {
                Text("Back to Login", color = Color(0xFF49A8FF))
            }
        }
    }
}
