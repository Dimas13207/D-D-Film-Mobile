package com.example.ddfilm.ui.screens

import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.example.ddfilm.data.api.SessionManager
import kotlinx.coroutines.flow.first

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    episodeId: Int,
    sessionManager: SessionManager,
    viewModel: PlayerViewModel = viewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.playbackState

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build()
    }

    LaunchedEffect(episodeId) {
        viewModel.fetchPlaybackInfo(episodeId)
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        when (val currentState = state) {
            is PlaybackState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is PlaybackState.Error -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Error: ${currentState.message}", color = Color.White)
                    Button(onClick = { viewModel.fetchPlaybackInfo(episodeId) }) {
                        Text("Retry")
                    }
                    Button(onClick = onBack) {
                        Text("Back")
                    }
                }
            }
            is PlaybackState.Success -> {
                val playback = currentState.data?.playback
                val streamUrl = playback?.streamUrl
                val subtitles = currentState.data?.subtitles ?: emptyList()

                LaunchedEffect(streamUrl) {
                    if (streamUrl != null) {
                        val token = sessionManager.accessToken.first()
                        
                        val dataSourceFactory = DefaultHttpDataSource.Factory()
                        if (token != null) {
                            dataSourceFactory.setDefaultRequestProperties(mapOf("Authorization" to "Bearer $token"))
                        }
                        
                        val mediaSourceFactory = DefaultMediaSourceFactory(context)
                            .setDataSourceFactory(dataSourceFactory)
                        
                        val subtitleConfigurations = subtitles.map { sub ->
                            MediaItem.SubtitleConfiguration.Builder(Uri.parse(sub.src))
                                .setMimeType(androidx.media3.common.MimeTypes.TEXT_VTT) // Media3 MimeTypes
                                .setLanguage(sub.lang)
                                .setLabel(sub.label)
                                .setSelectionFlags(androidx.media3.common.C.SELECTION_FLAG_DEFAULT)
                                .build()
                        }

                        val mediaItem = MediaItem.Builder()
                            .setUri(streamUrl)
                            .setSubtitleConfigurations(subtitleConfigurations)
                            .build()
                            
                        val mediaSource = mediaSourceFactory.createMediaSource(mediaItem)
                        
                        exoPlayer.setMediaSource(mediaSource)
                        exoPlayer.prepare()
                        exoPlayer.playWhenReady = true
                        
                        if ((currentState.data?.resumePositionSec ?: 0) > 0) {
                            exoPlayer.seekTo(currentState.data!!.resumePositionSec * 1000L)
                        }
                    }
                }

                AndroidView(
                    factory = {
                        PlayerView(it).apply {
                            player = exoPlayer
                            useController = true
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
