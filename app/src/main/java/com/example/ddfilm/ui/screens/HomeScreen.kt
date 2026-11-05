package com.example.ddfilm.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.ddfilm.data.api.SessionManager
import com.example.ddfilm.ui.components.CatalogItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    sessionManager: SessionManager,
    viewModel: HomeViewModel = viewModel(),
    onTitleClick: (Int) -> Unit,
    onProfileClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    val state by viewModel.catalogState
    val coinsRegular by sessionManager.coinsRegular.collectAsState(initial = 0)
    val coinsBonus by sessionManager.coinsBonus.collectAsState(initial = 0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("D&D Film", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = "Wallet",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${coinsRegular + coinsBonus}",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val currentState = state) {
                is CatalogState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is CatalogState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Error: ${currentState.message}")
                        Button(onClick = { viewModel.fetchCatalog() }) {
                            Text("Retry")
                        }
                    }
                }
                is CatalogState.Success -> {
                    val data = currentState.data
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        data?.sections?.let { sections ->
                            items(sections) { section ->
                                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                    Text(
                                        text = section.title,
                                        style = MaterialTheme.typography.titleLarge,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                        fontWeight = FontWeight.Bold
                                    )
                                    LazyRow(
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        items(section.items) { title ->
                                            CatalogItem(
                                                title = title,
                                                onClick = { onTitleClick(it.id) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Fallback if no sections but direct titles
                        data?.titles?.let { titles ->
                            item {
                                Text(
                                    text = "Semua Film",
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            items(titles.chunked(2)) { rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    rowItems.forEach { title ->
                                        CatalogItem(
                                            title = title,
                                            onClick = { onTitleClick(it.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
