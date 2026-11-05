package com.example.ddfilm.ui.screens

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ddfilm.data.api.RetrofitClient
import com.example.ddfilm.data.model.Title
import com.example.ddfilm.data.model.Episode
import kotlinx.coroutines.launch

class DetailsViewModel : ViewModel() {

    private val _titleState = mutableStateOf<DetailsState>(DetailsState.Loading)
    val titleState: State<DetailsState> = _titleState

    fun fetchTitleDetails(titleId: Int) {
        viewModelScope.launch {
            _titleState.value = DetailsState.Loading
            try {
                val response = RetrofitClient.apiService.getTitleDetails(titleId.toString())
                if (response.isSuccessful && response.body()?.ok == true) {
                    val data = response.body()?.data
                    if (data != null) {
                        _titleState.value = DetailsState.Success(data.title, data.episodes)
                    } else {
                        _titleState.value = DetailsState.Error("Data empty")
                    }
                } else {
                    _titleState.value = DetailsState.Error(response.body()?.error ?: "Unknown error")
                }
            } catch (e: Exception) {
                _titleState.value = DetailsState.Error(e.message ?: "Connection error")
            }
        }
    }

    fun purchaseEpisode(episodeId: Int, sessionManager: com.example.ddfilm.data.api.SessionManager, onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.purchaseEpisode(com.example.ddfilm.data.api.PurchaseRequest(episodeId))
                if (response.isSuccessful && response.body()?.ok == true) {
                    // Update wallet after purchase
                    val profileResponse = RetrofitClient.apiService.getProfile()
                    if (profileResponse.isSuccessful && profileResponse.body()?.ok == true) {
                        val wallet = profileResponse.body()?.data?.wallet
                        if (wallet != null) {
                            sessionManager.updateWallet(wallet.coinsRegular, wallet.coinsBonus)
                        }
                    }
                    onComplete(true, null)
                } else {
                    onComplete(false, response.body()?.error ?: "Purchase failed")
                }
            } catch (e: Exception) {
                onComplete(false, e.message ?: "Connection error")
            }
        }
    }
}

sealed class DetailsState {
    object Loading : DetailsState()
    data class Success(val title: Title, val episodes: List<Episode>) : DetailsState()
    data class Error(val message: String) : DetailsState()
}
