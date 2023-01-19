package se.magello.salesforce

import java.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import se.magello.serialization.InstantSerializer

/**
 * Salesforce Authorization response.
 *
 * @link https://help.salesforce.com/s/articleView?id=sf.remoteaccess_oauth_client_credentials_flow.htm&type=5
 */
@Serializable
data class SalesForceAuthResponse(
    @SerialName("access_token") val accessToken: String,
    val id: String,
    @SerialName("instance_url") val instanceUrl: String,
    @Serializable(with = InstantSerializer::class) @SerialName("issued_at") val issuedAt: Instant,
    val signature: String,
    @SerialName("token_type") val tokenType: String,
    val scope: String = ""
)

@Serializable
data class SalesForceVersion(val label: String, val url: String, val version: String)