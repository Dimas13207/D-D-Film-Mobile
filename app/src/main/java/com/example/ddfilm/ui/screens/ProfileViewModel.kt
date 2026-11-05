package com.example.ddfilm.ui.screens

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ddfilm.data.api.RetrofitClient
import com.example.ddfilm.data.api.SessionManager
import com.example.ddfilm.data.model.UserResponse
import kotlinx.coroutines.launch

class ProfileViewModel(private val sessionManager: SessionManager) : ViewModel() {

    private val _profileState = mutableStateOf<ProfileState>(ProfileState.Loading)
    val profileState: State<ProfileState> = _profileState

    init {
        fetchProfile()
    }

    fun fetchProfile() {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            try {
                val response = RetrofitClient.apiService.getProfile()
                if (response.isSuccessful && response.body()?.ok == true) {
                    val data = response.body()?.data
                    if (data != null) {
                        sessionManager.updateWallet(data.wallet?.coinsRegular ?: 0, data.wallet?.coinsBonus ?: 0)
                        _profileState.value = ProfileState.Success(data)
                    } else {
                        _profileState.value = ProfileState.Error("Data empty")
                    }
                } else {
                    _profileState.value = ProfileState.Error(response.body()?.error ?: "Unknown error")
                }
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error(e.message ?: "Connection error")
            }
        }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                // In a real app, we might call the logout API here
                sessionManager.clearSession()
                onComplete()
            } catch (e: Exception) {
                // Handle logout error if necessary
            }
        }
    }
}

sealed class ProfileState {
    object Loading : ProfileState()
    data class Success(val data: UserResponse) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

class ProfileViewModelFactory(private val sessionManager: SessionManager) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        return ProfileViewModel(sessionManager) as T
    }
}
