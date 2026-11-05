package com.example.ddfilm.ui.screens

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ddfilm.data.api.LoginRequest
import com.example.ddfilm.data.api.RetrofitClient
import com.example.ddfilm.data.api.SessionManager
import kotlinx.coroutines.launch

class LoginViewModel(private val sessionManager: SessionManager) : ViewModel() {

    private val _loginState = mutableStateOf<LoginState>(LoginState.Idle)
    val loginState: State<LoginState> = _loginState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val response = RetrofitClient.apiService.loginOrRegister(
                    LoginRequest(
                        action = "login",
                        email = email,
                        password = password,
                        deviceName = android.os.Build.MODEL
                    )
                )
                if (response.isSuccessful && response.body()?.ok == true) {
                    val tokenPair = response.body()?.data
                    if (tokenPair != null) {
                        sessionManager.saveSession(
                            accessToken = tokenPair.accessToken,
                            refreshToken = tokenPair.refreshToken,
                            userName = tokenPair.user.name
                        )
                        // Fetch profile to get initial wallet balance
                        try {
                            val profileResponse = RetrofitClient.apiService.getProfile()
                            if (profileResponse.isSuccessful && profileResponse.body()?.ok == true) {
                                val wallet = profileResponse.body()?.data?.wallet
                                if (wallet != null) {
                                    sessionManager.updateWallet(wallet.coinsRegular, wallet.coinsBonus)
                                }
                            }
                        } catch (e: Exception) {
                            // Ignore profile fetch error for now
                        }
                        _loginState.value = LoginState.Success
                    } else {
                        _loginState.value = LoginState.Error("Invalid response data")
                    }
                } else {
                    _loginState.value = LoginState.Error(response.body()?.error ?: "Login failed")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Connection error")
            }
        }
    }

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val response = RetrofitClient.apiService.loginOrRegister(
                    LoginRequest(
                        action = "register",
                        name = name,
                        email = email,
                        password = password,
                        deviceName = android.os.Build.MODEL
                    )
                )
                if (response.isSuccessful && response.body()?.ok == true) {
                    val tokenPair = response.body()?.data
                    if (tokenPair != null) {
                        sessionManager.saveSession(
                            accessToken = tokenPair.accessToken,
                            refreshToken = tokenPair.refreshToken,
                            userName = tokenPair.user.name
                        )
                        // Fetch profile to get initial wallet balance
                        try {
                            val profileResponse = RetrofitClient.apiService.getProfile()
                            if (profileResponse.isSuccessful && profileResponse.body()?.ok == true) {
                                val wallet = profileResponse.body()?.data?.wallet
                                if (wallet != null) {
                                    sessionManager.updateWallet(wallet.coinsRegular, wallet.coinsBonus)
                                }
                            }
                        } catch (e: Exception) {
                            // Ignore profile fetch error for now
                        }
                        _loginState.value = LoginState.Success
                    } else {
                        _loginState.value = LoginState.Error("Invalid response data")
                    }
                } else {
                    _loginState.value = LoginState.Error(response.body()?.error ?: "Registration failed")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Connection error")
            }
        }
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}
