package com.example.ddfilm.data.api

import com.example.ddfilm.data.model.*
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("auth/token")
    suspend fun loginOrRegister(
        @Body request: LoginRequest
    ): Response<ApiResponse<TokenPair>>

    @POST("auth/token/refresh")
    suspend fun refreshToken(
        @Body request: RefreshRequest
    ): Response<ApiResponse<TokenPair>>

    @DELETE("auth/token")
    suspend fun logout(
        @Body request: RefreshRequest
    ): Response<ApiResponse<Unit>>

    @GET("catalog")
    suspend fun getCatalog(
        @Query("sections") sections: String? = null,
        @Query("q") query: String? = null
    ): Response<ApiResponse<CatalogResponse>>

    @GET("catalog/search")
    suspend fun search(
        @Query("q") query: String
    ): Response<ApiResponse<List<Title>>>

    @GET("catalog/{idOrSlug}")
    suspend fun getTitleDetails(
        @Path("idOrSlug") idOrSlug: String
    ): Response<ApiResponse<TitleDetailsResponse>>

    @GET("auth/me")
    suspend fun getProfile(): Response<ApiResponse<UserResponse>>

    @POST("playback")
    suspend fun getPlaybackPermission(
        @Body request: PlaybackRequest
    ): Response<ApiResponse<PlaybackResponse>>

    @POST("purchase")
    suspend fun purchaseEpisode(
        @Body request: PurchaseRequest
    ): Response<ApiResponse<Unit>>

    @GET("wallet/transactions")
    suspend fun getTransactions(): Response<ApiResponse<List<Transaction>>>

    @GET("wallet/packages")
    suspend fun getTopUpPackages(): Response<ApiResponse<List<TopUpPackage>>>

    @POST("wallet/topup")
    suspend fun topUp(
        @Body request: TopUpRequest
    ): Response<ApiResponse<TopUpResponse>>
}

data class TopUpRequest(
    @SerializedName("packageId") val packageId: Int
)

data class PurchaseRequest(
    val episodeId: Int
)

data class LoginRequest(
    val action: String, // "login" or "register"
    val email: String,
    val password: String,
    val name: String? = null,
    val deviceName: String? = null
)

data class RefreshRequest(
    val refreshToken: String
)

data class PlaybackRequest(
    val episodeId: Int
)
