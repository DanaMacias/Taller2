package com.example.taller2.ui.screens.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taller2.GameActivity
import com.example.taller2.ui.screens.Register.RegisterActivity


class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoginScreen(
                onLoginSuccess = {
                    val intent = Intent(this, GameActivity::class.java)
                    startActivity(intent)
                },
                onCreateAccount = {
                    val intent = Intent(this, RegisterActivity::class.java)
                    startActivity(intent)
                }
            )
        }

    }
}



@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onLoginSuccess: () -> Unit = {},
    onCreateAccount: () -> Unit = {}
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val loginResult by viewModel.loginResult.collectAsState()
    val loginError by viewModel.loginError.collectAsState()


    LaunchedEffect(loginResult) {
        if (loginResult != null) {
            onLoginSuccess()
        }
    }

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

            Text("Login", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(32.dp))

            // Username
            TextField(
                value = username,
                onValueChange = { username = it },
                placeholder = { Text("Username", color = Color.Gray) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0x33FFFFFF),
                    unfocusedContainerColor = Color(0x22FFFFFF),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            // Password
            TextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Password", color = Color.Gray) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0x33FFFFFF),
                    unfocusedContainerColor = Color(0x22FFFFFF),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(Modifier.height(8.dp))


            if (loginError != null) {
                Text(
                    text = loginError!!,
                    color = Color.Red,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { viewModel.login(username, password) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sign In", color = Color.White)
            }

            Spacer(Modifier.height(16.dp))

            Text("OR", color = Color.Gray)

            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = onCreateAccount,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create Account", color = Color(0xFF49A8FF))
            }
        }
    }
}

