package com.example.taller2

import android.accounts.Account
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.taller2.ui.AccountScreen
import com.example.taller2.ui.ResultsActivity
import kotlinx.coroutines.launch
import com.example.taller2.ui.screens.room.RoomScreen

class GameActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GameNavigationDrawer()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameNavigationDrawer() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var currentScreen by remember { mutableStateOf("home") }

    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.background(Color(0xFF0D0B24)),
                drawerContainerColor = Color(0xFF0D0B24)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxHeight()
                ) {

                    Text("Menú", color = Color.White, style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(20.dp))

                    NavigationDrawerItem(
                        label = { Text("Inicio", color = Color.White) },
                        selected = currentScreen == "home",
                        onClick = {
                            currentScreen = "home"
                            scope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = Color(0xFF1E1B3C)
                        )
                    )

                    NavigationDrawerItem(
                        label = { Text("Salas", color = Color.White) },
                        selected = currentScreen == "rooms",
                        onClick = {
                            currentScreen = "rooms"
                            scope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = Color(0xFF1E1B3C)
                        )
                    )

                    NavigationDrawerItem(
                        label = { Text("Perfil", color = Color.White) },
                        selected = currentScreen == "Perfil",
                        onClick = {
                            currentScreen = "Perfil"
                            scope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = Color(0xFF1E1B3C)
                        )
                    )
                }
            }
        },
        drawerState = drawerState
    ) {
        Scaffold(
            containerColor = Color(0xFF0D0B24),
            topBar = {
                TopAppBar(
                    title = { Text("GameActivity", color = Color.White) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF1E1B3C)
                    ),
                    navigationIcon = {
                        TextButton(
                            onClick = {
                                scope.launch {
                                    if (drawerState.isClosed) drawerState.open()
                                    else drawerState.close()
                                }
                            }
                        ) {
                            Text("≡", color = Color.White)
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .background(Color(0xFF0D0B24))
            ) {
                when (currentScreen) {
                    "home" -> GameHomeScreen()
                    "rooms" -> RoomScreen()
                    "Perfil" -> AccountScreen { }
                }
            }
        }
    }
}



@Composable
fun GameHomeScreen() {

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            "Welcome!",
            color = Color.White,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(10.dp))

        Text(
            "Main game screen",
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(30.dp))

        Button(
            onClick = {
                val intent = Intent(context, ResultsActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7A3CFF))
        ) {
            Text("View Results", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}