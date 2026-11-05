package com.example.ddfilm.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ddfilm.data.api.RetrofitClient
import com.example.ddfilm.data.model.Title
import com.example.ddfilm.ui.components.CatalogItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {
    var searchQuery by mutableStateOf("")
    private val _searchState = mutableStateOf<SearchState>(SearchState.Idle)
    val searchState: State<SearchState> = _searchState
    
    private var searchJob: Job? = null

    fun onQueryChange(query: String) {
        searchQuery = query
        searchJob?.cancel()
        if (query.length >= 2) {
            searchJob = viewModelScope.launch {
                delay(500)
                performSearch(query)
            }
        } else if (query.isEmpty()) {
            _searchState.value = SearchState.Idle
        }
    }

    private suspend fun performSearch(query: String) {
        _searchState.value = SearchState.Loading
        try {
            val response = RetrofitClient.apiService.search(query)
            if (response.isSuccessful && response.body()?.ok == true) {
                val titles = response.body()?.data ?: emptyList()
                _searchState.value = SearchState.Success(titles)
            } else {
                _searchState.value = SearchState.Error(response.body()?.error ?: "Unknown error")
            }
        } catch (e: Exception) {
            _searchState.value = SearchState.Error(e.message ?: "Connection error")
        }
    }
}

sealed class SearchState {
    object Idle : SearchState()
    object Loading : SearchState()
    data class Success(val titles: List<Title>) : SearchState()
    data class Error(val message: String) : SearchState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onTitleClick: (Int) -> Unit,
    onBackClick: () -> Unit,
    viewModel: SearchViewModel = viewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = viewModel.searchQuery,
                        onValueChange = { viewModel.onQueryChange(it) },
                        placeholder = { Text("Cari film...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            disabledContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                        ),
                        trailingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        }
                    )
                },
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
            when (val state = viewModel.searchState.value) {
                is SearchState.Idle -> {
                    Text(
                        "Masukkan setidaknya 2 karakter untuk mencari",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                is SearchState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is SearchState.Error -> {
                    Text(
                        "Error: ${state.message}",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                is SearchState.Success -> {
                    if (state.titles.isEmpty()) {
                        Text(
                            "Tidak ada hasil ditemukan",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.titles) { title ->
                                Card(
                                    onClick = { onTitleClick(title.id) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Simplified item for search results
                                        CatalogItem(
                                            title = title,
                                            onClick = { onTitleClick(it.id) },
                                            modifier = Modifier.size(80.dp, 120.dp)
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Text(
                                                title.nameId,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            title.synopsisId?.let {
                                                Text(
                                                    it,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    maxLines = 2
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
    }
}
