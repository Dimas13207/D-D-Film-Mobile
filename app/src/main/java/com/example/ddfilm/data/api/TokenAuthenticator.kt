package com.example.ddfilm.data.api

import com.example.ddfilm.data.model.ApiResponse
import com.example.ddfilm.data.api.RefreshRequest
import com.example.ddfilm.data.model.TokenPair
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val sessionManager: SessionManager,
    private val apiService: Lazy<ApiService>
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        val refreshToken = runBlocking {
            sessionManager.refreshToken.first()
        }

        if (refreshToken == null) return null

        synchronized(this) {
            val newTokenPair = runBlocking {
                try {
                    val res = apiService.value.refreshToken(RefreshRequest(refreshToken))
                    if (res.isSuccessful && res.body()?.ok == true) {
                        res.body()?.data
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    null
                }
            }

            return if (newTokenPair != null) {
                runBlocking {
                    sessionManager.saveSession(
                        accessToken = newTokenPair.accessToken,
                        refreshToken = newTokenPair.refreshToken,
                        userName = newTokenPair.user.name
                    )
                }
                response.request.newBuilder()
                    .header("Authorization", "Bearer ${newTokenPair.accessToken}")
                    .build()
            } else {
                runBlocking {
                    sessionManager.clearSession()
                }
                null
            }
        }
    }
}
