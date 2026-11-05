package com.example.ddfilm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import com.example.ddfilm.data.api.RetrofitClient
import com.example.ddfilm.data.api.SessionManager
import com.example.ddfilm.ui.navigation.NavGraph
import com.example.ddfilm.ui.theme.DDFilmTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Retrofit with AuthInterceptor
        val sessionManager = SessionManager(this)
        RetrofitClient.init(sessionManager)

        enableEdgeToEdge()
        setContent {
            val themeMode by sessionManager.themeMode.collectAsState(initial = "system")
            val darkTheme = when (themeMode) {
                "light" -> false
                "dark" -> true
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }
            
            DDFilmTheme(darkTheme = darkTheme) {
                val navController = rememberNavController()
                NavGraph(navController = navController, sessionManager = sessionManager)
            }
        }
    }
}
