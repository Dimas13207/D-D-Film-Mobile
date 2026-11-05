package com.example.ddfilm.ui.screens

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ddfilm.data.api.RetrofitClient
import com.example.ddfilm.data.api.SessionManager
import com.example.ddfilm.data.api.TopUpRequest
import com.example.ddfilm.data.model.TopUpPackage
import kotlinx.coroutines.launch

class TopUpViewModel(private val sessionManager: SessionManager) : ViewModel() {

    private val _packagesState = mutableStateOf<TopUpState>(TopUpState.Loading)
    val packagesState: State<TopUpState> = _packagesState

    private val _topUpResult = mutableStateOf<TopUpResult?>(null)
    val topUpResult: State<TopUpResult?> = _topUpResult

    init {
        fetchPackages()
    }

    fun fetchPackages() {
        viewModelScope.launch {
            _packagesState.value = TopUpState.Loading
            try {
                val response = RetrofitClient.apiService.getTopUpPackages()
                if (response.isSuccessful && response.body()?.ok == true) {
                    _packagesState.value = TopUpState.Success(response.body()?.data ?: emptyList())
                } else {
                    _packagesState.value = TopUpState.Error(response.body()?.error ?: "Unknown error")
                }
            } catch (e: Exception) {
                _packagesState.value = TopUpState.Error(e.message ?: "Connection error")
            }
        }
    }

    fun selectPackage(pkg: TopUpPackage) {
        viewModelScope.launch {
            _topUpResult.value = TopUpResult.Loading
            try {
                val response = RetrofitClient.apiService.topUp(TopUpRequest(pkg.id))
                if (response.isSuccessful && response.body()?.ok == true) {
                    val data = response.body()?.data
                    if (data != null) {
                        _topUpResult.value = TopUpResult.Success(data.paymentUrl)
                    } else {
                        _topUpResult.value = TopUpResult.Error("Payment initiation failed")
                    }
                } else {
                    _topUpResult.value = TopUpResult.Error(response.body()?.error ?: "Unknown error")
                }
            } catch (e: Exception) {
                _topUpResult.value = TopUpResult.Error(e.message ?: "Connection error")
            }
        }
    }

    fun clearResult() {
        _topUpResult.value = null
    }
}

sealed class TopUpState {
    object Loading : TopUpState()
    data class Success(val packages: List<TopUpPackage>) : TopUpState()
    data class Error(val message: String) : TopUpState()
}

sealed class TopUpResult {
    object Loading : TopUpResult()
    data class Success(val paymentUrl: String) : TopUpResult()
    data class Error(val message: String) : TopUpResult()
}

class TopUpViewModelFactory(private val sessionManager: SessionManager) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        return TopUpViewModel(sessionManager) as T
    }
}
