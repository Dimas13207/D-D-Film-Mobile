package com.example.ddfilm.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.ddfilm.data.model.Transaction
import kotlinx.coroutines.launch

class TransactionViewModel : ViewModel() {
    private val _state = mutableStateOf<TransactionState>(TransactionState.Loading)
    val state: State<TransactionState> = _state

    init {
        fetchTransactions()
    }

    fun fetchTransactions() {
        viewModelScope.launch {
            _state.value = TransactionState.Loading
            try {
                val response = RetrofitClient.apiService.getTransactions()
                if (response.isSuccessful && response.body()?.ok == true) {
                    _state.value = TransactionState.Success(response.body()?.data ?: emptyList())
                } else {
                    _state.value = TransactionState.Error(response.body()?.error ?: "Unknown error")
                }
            } catch (e: Exception) {
                _state.value = TransactionState.Error(e.message ?: "Connection error")
            }
        }
    }
}

sealed class TransactionState {
    object Loading : TransactionState()
    data class Success(val transactions: List<Transaction>) : TransactionState()
    data class Error(val message: String) : TransactionState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    sessionManager: com.example.ddfilm.data.api.SessionManager,
    onBackClick: () -> Unit,
    viewModel: TransactionViewModel = viewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Riwayat Transaksi") },
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
            when (val currentState = viewModel.state.value) {
                is TransactionState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is TransactionState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Error: ${currentState.message}")
                        Button(onClick = { viewModel.fetchTransactions() }) {
                            Text("Retry")
                        }
                    }
                }
                is TransactionState.Success -> {
                    if (currentState.transactions.isEmpty()) {
                        Text(
                            "Belum ada transaksi",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(currentState.transactions) { transaction ->
                                TransactionItem(transaction)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = transaction.createdAt,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Tipe: ${transaction.type}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                val total = transaction.amountRegular + transaction.amountBonus
                Text(
                    text = if (total > 0) "+$total" else "$total",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = if (total > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                Text(
                    text = "Koin",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
