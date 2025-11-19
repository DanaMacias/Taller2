package com.example.taller2.ui.screens.Register

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import com.example.taller2.ui.screens.login.LoginActivity
import androidx.lifecycle.viewmodel.compose.viewModel

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            val viewModel: RegisterViewModel = viewModel()

            RegisterScreen(
                viewModel = viewModel,
                onBackToLogin = {
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            )
        }
    }
}

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onBackToLogin: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val registerSuccess by viewModel.registerSuccess.collectAsState()
    val registerError by viewModel.registerError.collectAsState()

    val fieldsEnabled = !registerSuccess
    val buttonEnabled = !registerSuccess

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
                onValueChange = { if (fieldsEnabled) fullName = it },
                placeholder = { Text("Full Name", color = Color.Gray) },
                enabled = fieldsEnabled,
                colors = fieldColors(),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))


            TextField(
                value = email,
                onValueChange = { if (fieldsEnabled) email = it },
                placeholder = { Text("Email", color = Color.Gray) },
                enabled = fieldsEnabled,
                colors = fieldColors(),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))


            TextField(
                value = password,
                onValueChange = { if (fieldsEnabled) password = it },
                placeholder = { Text("Password", color = Color.Gray) },
                enabled = fieldsEnabled,
                colors = fieldColors(),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))


            TextField(
                value = confirmPassword,
                onValueChange = { if (fieldsEnabled) confirmPassword = it },
                placeholder = { Text("Confirm Password", color = Color.Gray) },
                enabled = fieldsEnabled,
                colors = fieldColors(),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))


            if (registerError != null) {
                Text(
                    text = registerError!!,
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
            }


            if (registerSuccess) {
                Text(
                    text = "Successful registration",
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))


            Button(
                onClick = {
                    if (password != confirmPassword) {
                        viewModel.setError("Passwords do not match")
                    } else if (fullName.isBlank() || email.isBlank() || password.isBlank()) {
                        viewModel.setError("All fields are required")
                    } else {
                        viewModel.register(fullName, email, password)
                    }
                },
                enabled = buttonEnabled,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (buttonEnabled) Color(0xFF7A3CFF) else Color.Gray
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

@Composable
fun fieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color(0x33FFFFFF),
    unfocusedContainerColor = Color(0x22FFFFFF),
    disabledContainerColor = Color(0x11FFFFFF),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    disabledTextColor = Color.LightGray,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent
)
