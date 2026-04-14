package yug.ramoliya.ojtapp.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import yug.ramoliya.ojtapp.ui.screens.LoginScreen
import yug.ramoliya.ojtapp.ui.screens.MainShellScreen
import yug.ramoliya.ojtapp.ui.screens.RegisterScreen

@Composable
fun StudentApp(vm: StudentAppViewModel) {
    val nav = rememberNavController()
    val message by vm.uiMessage.collectAsState()

    if (message != null) {
        AlertDialog(
            onDismissRequest = { vm.clearMessage() },
            confirmButton = {
                TextButton(onClick = { vm.clearMessage() }) { Text("OK") }
            },
            title = { Text("Notice") },
            text = { Text(message ?: "") },
        )
    }

    NavHost(
        navController = nav,
        startDestination = vm.startRoute,
        modifier = Modifier.fillMaxSize(),
    ) {
        composable("login") {
            LoginScreen(
                vm = vm,
                onRegister = { nav.navigate("register") },
                onLoggedIn = {
                    nav.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                },
            )
        }
        composable("register") {
            RegisterScreen(
                vm = vm,
                onBack = { nav.popBackStack() },
                onRegistered = {
                    nav.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                },
            )
        }
        composable("main") {
            MainShellScreen(
                vm = vm,
                onLogout = {
                    nav.navigate("login") {
                        popUpTo("main") { inclusive = true }
                    }
                },
            )
        }
    }
}
