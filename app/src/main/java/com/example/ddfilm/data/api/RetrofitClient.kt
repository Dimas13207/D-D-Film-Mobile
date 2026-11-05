package com.example.ddfilm.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://3000-ii92yd7z8vwagjuoxhwc8.e2b.app/api/v1/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private var authInterceptor: AuthInterceptor? = null
    private var tokenAuthenticator: TokenAuthenticator? = null

    fun init(sessionManager: SessionManager) {
        authInterceptor = AuthInterceptor(sessionManager)
        tokenAuthenticator = TokenAuthenticator(sessionManager, lazy { apiService })
    }

    private val client: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .addInterceptor(logging)
        
        authInterceptor?.let {
            builder.addInterceptor(it)
        }
        
        tokenAuthenticator?.let {
            builder.authenticator(it)
        }
        
        builder.build()
    }

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(ApiService::class.java)
    }
}
