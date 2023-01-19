package se.magello.cinode

import java.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CinodeAuthResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @Transient val createdAt: String = Instant.now().toString()
)

@Serializable
data class CinodeSkill(
    val companyId: Int,
    val companyUserId: Int,
    val favourite: Boolean?,
    val id: Int,
    val keyword: Keyword?,
    val level: Int?,
    val levelGoal: Int?,
    val levelGoalDeadline: String?,
    val numberOfDaysWorkExperience: Int?,
    val profileId: Int?
)

@Serializable
data class Keyword(
    val id: Int?,
    val masterSynonym: String?,
    val masterSynonymId: Int?,
    val synonyms: List<String>,
    val type: Int?,
    val universal: Boolean?,
    val verified: Boolean?
)

@Serializable
data class CinodeUser(
    val companyUserEmail: String?,
    val companyUserId: Int,
    val companyUserType: Int?,
    val createdDateTime: String,
    val firstName: String,
    val homeAddress: HomeAddress?,
    val image: Image?,
    val lastName: String,
    val title: String?,
    val updatedDateTime: String?
)

@Serializable
data class HomeAddress(
    val city: String?,
    val country: String?,
    val countryCode: String?,
    val displayName: String?,
    val formattedAddress: String?,
    val latitude: String?,
    val locationId: Int?,
    val longitude: String?,
    val name: String?,
    val phoneNumber: String?,
    val street: String?,
    val streetNumber: String?,
    val webSiteUrl: String?,
    val zipCode: String?
)

@Serializable
data class Image(
    val companyId: Int?,
    val imageId: Int?,
    val largeImageUrl: String?,
    val uploadedWhen: String?,
    val url: String?
)
