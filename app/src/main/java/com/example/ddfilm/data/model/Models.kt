package com.example.ddfilm.data.model

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("ok") val ok: Boolean,
    @SerializedName("error") val error: String? = null,
    @SerializedName("data") val data: T? = null
)

data class TokenPair(
    @SerializedName("tokenType") val tokenType: String,
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String,
    @SerializedName("expiresIn") val expiresIn: Int,
    @SerializedName("user") val user: User,
    @SerializedName("roles") val roles: List<String>,
    @SerializedName("permissions") val permissions: List<String>
)

data class User(
    @SerializedName("id") val id: Int,
    @SerializedName("email") val email: String,
    @SerializedName("name") val name: String,
    @SerializedName("language") val language: String
)

data class Title(
    @SerializedName("id") val id: Int,
    @SerializedName("slug") val slug: String,
    @SerializedName("type") val type: String, // "series", "film", etc.
    @SerializedName("nameId") val nameId: String,
    @SerializedName("nameEn") val nameEn: String?,
    @SerializedName("synopsisId") val synopsisId: String?,
    @SerializedName("year") val year: Int?,
    @SerializedName("durationMin") val durationMin: Int?,
    @SerializedName("ageRating") val ageRating: String?,
    @SerializedName("posterUrl") val posterUrl: String?,
    @SerializedName("heroUrl") val heroUrl: String?,
    @SerializedName("bundlePriceCoins") val bundlePriceCoins: Int?,
    @SerializedName("hasTrailer") val hasTrailer: Boolean
)

data class Episode(
    @SerializedName("id") val id: Int,
    @SerializedName("number") val number: Int,
    @SerializedName("nameId") val nameId: String,
    @SerializedName("durationMin") val durationMin: Int?,
    @SerializedName("priceCoins") val priceCoins: Int,
    @SerializedName("isFree") val isFree: Boolean,
    @SerializedName("access") val access: String // "free", "owned", "locked"
)

data class PlaybackResponse(
    @SerializedName("playback") val playback: PlaybackInfo?,
    @SerializedName("episode") val episode: Episode?,
    @SerializedName("nextEpisode") val nextEpisode: Episode?,
    @SerializedName("resumePositionSec") val resumePositionSec: Int,
    @SerializedName("subtitles") val subtitles: List<Subtitle>
)

data class PlaybackInfo(
    @SerializedName("playbackToken") val playbackToken: String,
    @SerializedName("streamUrl") val streamUrl: String?
)

data class Subtitle(
    @SerializedName("id") val id: Int,
    @SerializedName("lang") val lang: String,
    @SerializedName("label") val label: String,
    @SerializedName("src") val src: String
)

data class CatalogResponse(
    @SerializedName("sections") val sections: List<CatalogSection>? = null,
    @SerializedName("titles") val titles: List<Title>? = null
)

data class CatalogSection(
    @SerializedName("title") val title: String,
    @SerializedName("items") val items: List<Title>
)

data class TitleDetailsResponse(
    @SerializedName("title") val title: Title,
    @SerializedName("episodes") val episodes: List<Episode>
)

data class UserResponse(
    @SerializedName("user") val user: User,
    @SerializedName("wallet") val wallet: WalletInfo?
)

data class WalletInfo(
    @SerializedName("coinsRegular") val coinsRegular: Int,
    @SerializedName("coinsBonus") val coinsBonus: Int
)

data class Transaction(
    @SerializedName("id") val id: Int,
    @SerializedName("amountRegular") val amountRegular: Int,
    @SerializedName("amountBonus") val amountBonus: Int,
    @SerializedName("type") val type: String, // "purchase", "topup", etc.
    @SerializedName("description") val description: String,
    @SerializedName("createdAt") val createdAt: String
)

data class TopUpPackage(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("coinsRegular") val coinsRegular: Int,
    @SerializedName("coinsBonus") val coinsBonus: Int,
    @SerializedName("priceAmount") val priceAmount: Double,
    @SerializedName("priceCurrency") val priceCurrency: String,
    @SerializedName("formattedPrice") val formattedPrice: String
)

data class TopUpResponse(
    @SerializedName("paymentUrl") val paymentUrl: String,
    @SerializedName("transactionId") val transactionId: String
)
