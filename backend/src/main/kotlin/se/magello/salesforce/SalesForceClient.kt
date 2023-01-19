package se.magello.salesforce

import com.typesafe.config.Config
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.resources.Resources
import io.ktor.client.plugins.resources.get
import io.ktor.client.request.accept
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.forms.submitForm
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.http.URLProtocol
import io.ktor.http.path
import io.ktor.serialization.kotlinx.json.json
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import se.magello.salesforce.requests.SalesForceRequest
import se.magello.salesforce.responses.QueryResponse
import se.magello.salesforce.responses.RecordType

private val logger = KotlinLogging.logger {}

private const val API_VERSION = "v57.0"

class SalesForceClient(config: Config) {
    object Endpoints {
        const val TOKEN = "/services/oauth2/token"
    }

    private val clientId: String
    private val clientSecret: String
    private val sessionTimeout: Long

    private var authResponse: SalesForceAuthResponse? = null

    private val httpClient: HttpClient

    init {
        clientId = config.getString("clientId")
        clientSecret = config.getString("clientSecret")
        val apiHost = config.getString("apiHost")
        sessionTimeout = config.getLong("sessionTimeout")

        httpClient = HttpClient(OkHttp) {
            install(Resources)
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.INFO
            }
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
            defaultRequest {
                host = apiHost
                port = 443
                url { protocol = URLProtocol.HTTPS }
            }
        }
    }

    private suspend fun fetchToken() {
        logger.debug { "Fetching token" }

        val req = httpClient.submitForm(formParameters = Parameters.build {
            append("grant_type", "client_credentials")
            append("client_id", clientId)
            append("client_secret", clientSecret)
        }) {
            url.path(Endpoints.TOKEN)
            accept(ContentType.Application.Json)
        }

        val response = if (req.status == HttpStatusCode.OK) {
            req.body<SalesForceAuthResponse>()
        } else {
            logger.error { "Failed to Salesforce access token, got status ${req.status}" }

            null
        }

        logger.info { "Got Auth response: $response" }

        authResponse = response
    }

    private suspend fun getOrRefreshToken(): SalesForceAuthResponse? {
        val expirationTime = authResponse?.issuedAt?.plus(sessionTimeout, ChronoUnit.HOURS)

        val now = Instant.now()

        if (expirationTime == null || now.isAfter(expirationTime)) {
            fetchToken()
        }

        return authResponse
    }

    /**
     * We are using SalesForce Object Query Language to fetch users from the table *Avtal__c*.
     *
     * TODO Could use this to immediately find all users
     */
    suspend fun getAllMagelloUsers(): QueryResponse? {
        val authResponse = getOrRefreshToken() ?: return null

        val response = httpClient.get(
            SalesForceRequest.APIVersion.Query(
                parent = SalesForceRequest.APIVersion(version = API_VERSION),
                q = RecordType.QUERY
            )
        ) {
            accept(ContentType.Application.Json)
            bearerAuth(authResponse.accessToken)
        }

        return if (response.status == HttpStatusCode.OK) {
            response.body()
        } else {
            val errorBody = response.body<String>()

            logger.warn { "Got ${response.status} when trying to find users from SalesForce with body ['$errorBody']" }

            null
        }
    }

    /**
     * Fetches a list of supported SalesForce versions. This can be used to make sure SalesForce supports the API
     * version we target.
     *
     * @see [SalesForceRequest.APIVersion].
     */
    suspend fun getVersions(): List<SalesForceVersion> {
        val authResponse = getOrRefreshToken() ?: return emptyList()

        val req = httpClient.get(SalesForceRequest()) {
            bearerAuth(authResponse.accessToken)
            accept(ContentType.Application.Json)
        }

        val versions: List<SalesForceVersion> = if (req.status == HttpStatusCode.OK) {
            req.body()
        } else {
            emptyList()
        }

        return versions
    }
}