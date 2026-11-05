package com.example.ddfilm.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ddfilm.data.api.SessionManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    sessionManager: SessionManager,
    onBack: () -> Unit
) {
    val themeMode by sessionManager.themeMode.collectAsState(initial = "system")
    val language by sessionManager.language.collectAsState(initial = "id")
    val scope = rememberCoroutineScope()

    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengaturan") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ListItem(
                headlineContent = { Text("Tema") },
                supportingContent = {
                    val label = when (themeMode) {
                        "light" -> "Terang"
                        "dark" -> "Gelap"
                        else -> "Ikuti Sistem"
                    }
                    Text(label)
                },
                leadingContent = { Icon(Icons.Default.Palette, contentDescription = null) },
                modifier = Modifier.clickable { showThemeDialog = true }
            )
            
            ListItem(
                headlineContent = { Text("Bahasa") },
                supportingContent = {
                    val label = when (language) {
                        "en" -> "English"
                        else -> "Bahasa Indonesia"
                    }
                    Text(label)
                },
                leadingContent = { Icon(Icons.Default.Language, contentDescription = null) },
                modifier = Modifier.clickable { showLanguageDialog = true }
            )
        }
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Pilih Tema") },
            text = {
                Column {
                    ThemeOption("Sistem", "system", themeMode) {
                        scope.launch { sessionManager.setThemeMode("system") }
                        showThemeDialog = false
                    }
                    ThemeOption("Terang", "light", themeMode) {
                        scope.launch { sessionManager.setThemeMode("light") }
                        showThemeDialog = false
                    }
                    ThemeOption("Gelap", "dark", themeMode) {
                        scope.launch { sessionManager.setThemeMode("dark") }
                        showThemeDialog = false
                    }
                }
            },
            confirmButton = {}
        )
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("Pilih Bahasa") },
            text = {
                Column {
                    LanguageOption("Bahasa Indonesia", "id", language) {
                        scope.launch { sessionManager.setLanguage("id") }
                        showLanguageDialog = false
                    }
                    LanguageOption("English", "en", language) {
                        scope.launch { sessionManager.setLanguage("en") }
                        showLanguageDialog = false
                    }
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
fun ThemeOption(label: String, value: String, current: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = value == current, onClick = onClick)
        Spacer(Modifier.width(8.dp))
        Text(label)
    }
}

@Composable
fun LanguageOption(label: String, value: String, current: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = value == current, onClick = onClick)
        Spacer(Modifier.width(8.dp))
        Text(label)
    }
}
