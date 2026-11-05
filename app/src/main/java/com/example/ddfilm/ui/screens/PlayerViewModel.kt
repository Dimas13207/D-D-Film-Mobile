package com.example.ddfilm.ui.screens

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ddfilm.data.api.PlaybackRequest
import com.example.ddfilm.data.api.RetrofitClient
import com.example.ddfilm.data.model.PlaybackResponse
import kotlinx.coroutines.launch

class PlayerViewModel : ViewModel() {

    private val _playbackState = mutableStateOf<PlaybackState>(PlaybackState.Loading)
    val playbackState: State<PlaybackState> = _playbackState

    fun fetchPlaybackInfo(episodeId: Int) {
        viewModelScope.launch {
            _playbackState.value = PlaybackState.Loading
            try {
                val response = RetrofitClient.apiService.getPlaybackPermission(
                    PlaybackRequest(episodeId = episodeId)
                )
                if (response.isSuccessful && response.body()?.ok == true) {
                    _playbackState.value = PlaybackState.Success(response.body()?.data)
                } else {
                    _playbackState.value = PlaybackState.Error(response.body()?.error ?: "Access denied")
                }
            } catch (e: Exception) {
                _playbackState.value = PlaybackState.Error(e.message ?: "Connection error")
            }
        }
    }
}

sealed class PlaybackState {
    object Loading : PlaybackState()
    data class Success(val data: PlaybackResponse?) : PlaybackState()
    data class Error(val message: String) : PlaybackState()
}
