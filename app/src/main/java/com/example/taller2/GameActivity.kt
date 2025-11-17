package com.example.taller2

import android.accounts.Account
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.platform.LocalContext
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
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {

                    Text("Menú", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(20.dp))

                    NavigationDrawerItem(
                        label = { Text("Inicio") },
                        selected = currentScreen == "home",
                        onClick = {
                            currentScreen = "home"
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        label = { Text("Salas") },
                        selected = currentScreen == "rooms",
                        onClick = {
                            currentScreen = "rooms"
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                    NavigationDrawerItem(
                        label = { Text("Perfil") },
                        selected = currentScreen == "Perfil",
                        onClick = {
                            currentScreen = "Perfil"
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        },
        drawerState = drawerState
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("GameActivity") },
                    navigationIcon = {
                        TextButton(
                            onClick = {
                                scope.launch {
                                    if (drawerState.isClosed) drawerState.open()
                                    else drawerState.close()
                                }
                            }
                        ) {
                            Text("≡")   // Menu estilo texto
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (currentScreen) {
                    "home" -> GameHomeScreen()
                    "rooms" -> RoomScreen()
                    "Perfil" -> AccountScreen {  }
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
        Text("Welcome!", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(10.dp))
        Text("Main game screen")

        Spacer(Modifier.height(30.dp))


        Button(
            onClick = {
                val intent = Intent(context, ResultsActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("View Results", color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

