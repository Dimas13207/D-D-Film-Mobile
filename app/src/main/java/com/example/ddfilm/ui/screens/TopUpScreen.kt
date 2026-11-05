package com.example.ddfilm.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ddfilm.data.api.SessionManager
import com.example.ddfilm.data.model.TopUpPackage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopUpScreen(
    sessionManager: SessionManager,
    onBack: () -> Unit
) {
    val viewModel: TopUpViewModel = viewModel(factory = TopUpViewModelFactory(sessionManager))
    val packagesState by viewModel.packagesState
    val topUpResult by viewModel.topUpResult
    val context = LocalContext.current

    LaunchedEffect(topUpResult) {
        if (topUpResult is TopUpResult.Success) {
            val url = (topUpResult as TopUpResult.Success).paymentUrl
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
            viewModel.clearResult()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Isi Ulang Koin") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (packagesState) {
                is TopUpState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is TopUpState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text((packagesState as TopUpState.Error).message, color = Color.Red)
                        Button(onClick = { viewModel.fetchPackages() }) {
                            Text("Coba Lagi")
                        }
                    }
                }
                is TopUpState.Success -> {
                    val packages = (packagesState as TopUpState.Success).packages
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                "Pilih Paket Koin",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        items(packages) { pkg ->
                            TopUpPackageItem(pkg = pkg) {
                                viewModel.selectPackage(pkg)
                            }
                        }
                    }
                }
            }

            if (topUpResult is TopUpResult.Loading) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black.copy(alpha = 0.5f)
                ) {
                    CircularProgressIndicator(modifier = Modifier.wrapContentSize(Alignment.Center))
                }
            }

            if (topUpResult is TopUpResult.Error) {
                AlertDialog(
                    onDismissRequest = { viewModel.clearResult() },
                    title = { Text("Gagal") },
                    text = { Text((topUpResult as TopUpResult.Error).message) },
                    confirmButton = {
                        TextButton(onClick = { viewModel.clearResult() }) {
                            Text("OK")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun TopUpPackageItem(pkg: TopUpPackage, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    pkg.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${pkg.coinsRegular} Koin",
                        fontSize = 14.sp
                    )
                    if (pkg.coinsBonus > 0) {
                        Text(
                            " + ${pkg.coinsBonus} Bonus",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
            Text(
                pkg.formattedPrice,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
