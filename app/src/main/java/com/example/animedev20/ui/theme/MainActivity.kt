package com.example.animedev20.ui.theme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.animedev20.ui.theme.navigation.AppNavHost
import com.example.animedev20.ui.theme.navigation.BottomNavigationBar
import com.example.animedev20.ui.theme.navigation.Screen
import com.example.animedev20.ui.theme.theme.AnimeDevTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AnimeDevTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val shouldShowBottomBar = when (navBackStackEntry?.destination?.route) {
        Screen.Home.route,
        Screen.Favorites.route,
        Screen.Trivia.route,
        Screen.Settings.route,
        Screen.Profile.route -> true
        else -> false
    }
    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                BottomNavigationBar(navController = navController)
            }
        }
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
