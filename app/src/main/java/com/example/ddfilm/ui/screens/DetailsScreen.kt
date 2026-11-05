package com.example.ddfilm.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.ddfilm.data.api.SessionManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    titleId: Int,
    sessionManager: SessionManager,
    viewModel: DetailsViewModel = viewModel(),
    onBackClick: () -> Unit,
    onEpisodeClick: (Int) -> Unit
) {
    val state by viewModel.titleState
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedEpisodeForPurchase by remember { mutableStateOf<com.example.ddfilm.data.model.Episode?>(null) }

    LaunchedEffect(titleId) {
        viewModel.fetchTitleDetails(titleId)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Detail Film") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                is DetailsState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is DetailsState.Error -> {
                    Text(
                        text = "Error: ${currentState.message}",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is DetailsState.Success -> {
                    val title = currentState.title
                    val episodes = currentState.episodes

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            AsyncImage(
                                model = title.heroUrl ?: title.posterUrl,
                                contentDescription = title.nameId,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(250.dp),
                                contentScale = ContentScale.Crop
                            )
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = title.nameId,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${title.year ?: ""} • ${title.ageRating ?: ""} • ${title.type}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = title.synopsisId ?: "Tidak ada sinopsis.",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }

                        item {
                            Text(
                                text = "Episode",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(16.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        items(episodes) { episode ->
                            val canPlay = episode.isFree || episode.access == "owned" || episode.access == "free"
                            
                            ListItem(
                                headlineContent = { Text("Episode ${episode.number}: ${episode.nameId}") },
                                supportingContent = { 
                                    val priceText = if (episode.isFree) "Gratis" else "${episode.priceCoins} Koin"
                                    val statusText = if (episode.access == "owned") "Dimiliki" else priceText
                                    Text("${episode.durationMin ?: 0} min • $statusText") 
                                },
                                trailingContent = {
                                    if (canPlay) {
                                        IconButton(onClick = { onEpisodeClick(episode.id) }) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = "Play")
                                        }
                                    } else {
                                        Button(onClick = { selectedEpisodeForPurchase = episode }) {
                                            Icon(Icons.Default.Lock, contentDescription = "Locked", modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Beli")
                                        }
                                    }
                                },
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
        }
    }

    if (selectedEpisodeForPurchase != null) {
        val episode = selectedEpisodeForPurchase!!
        AlertDialog(
            onDismissRequest = { selectedEpisodeForPurchase = null },
            title = { Text("Beli Episode") },
            text = { Text("Apakah Anda yakin ingin membeli Episode ${episode.number} seharga ${episode.priceCoins} koin?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.purchaseEpisode(episode.id, sessionManager) { success, error ->
                        if (success) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Berhasil membeli episode!")
                                viewModel.fetchTitleDetails(titleId)
                            }
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("Gagal: $error")
                            }
                        }
                        selectedEpisodeForPurchase = null
                    }
                }) {
                    Text("Beli Sekarang")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedEpisodeForPurchase = null }) {
                    Text("Batal")
                }
            }
        )
    }
}
