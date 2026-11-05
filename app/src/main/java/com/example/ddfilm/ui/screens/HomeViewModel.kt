package com.example.ddfilm.ui.screens

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ddfilm.data.api.RetrofitClient
import com.example.ddfilm.data.model.CatalogResponse
import com.example.ddfilm.data.model.Title
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _catalogState = mutableStateOf<CatalogState>(CatalogState.Loading)
    val catalogState: State<CatalogState> = _catalogState

    init {
        fetchCatalog()
    }

    fun fetchCatalog() {
        viewModelScope.launch {
            _catalogState.value = CatalogState.Loading
            try {
                val response = RetrofitClient.apiService.getCatalog(sections = "all")
                if (response.isSuccessful && response.body()?.ok == true) {
                    _catalogState.value = CatalogState.Success(response.body()?.data)
                } else {
                    _catalogState.value = CatalogState.Error(response.body()?.error ?: "Unknown error")
                }
            } catch (e: Exception) {
                _catalogState.value = CatalogState.Error(e.message ?: "Connection error")
            }
        }
    }
}

sealed class CatalogState {
    object Loading : CatalogState()
    data class Success(val data: CatalogResponse?) : CatalogState()
    data class Error(val message: String) : CatalogState()
}
