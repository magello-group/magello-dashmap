package se.magello.cinode

import com.auth0.jwt.JWT
import com.sksamuel.aedile.core.caffeineBuilder
import com.typesafe.config.Config
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequest
import io.ktor.client.request.basicAuth
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.path
import io.ktor.serialization.kotlinx.json.json
import java.time.Instant
import kotlinx.serialization.json.Json
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

private const val BASE_URL = "https://api.cinode.com"

class CinodeClient(
    config: Config,
) {
    private val accessSecret: String
    private val accessId: String

    private val companyId = 150

    private var authToken: CinodeAuthResponse? = null

    private val httpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
        install(HttpRequestRetry) {
            delayMillis {
                val delay = (response?.headers?.get("x-ratelimit-reset")?.toLongOrNull()?.times(1000)
                    ?: (it * 3000).toLong())
                logger.info { "Ran into rate limit, trying again in $delay milliseconds" }
                delay
            }
            retryIf(5) { _: HttpRequest, httpResponse: HttpResponse ->
                if (httpResponse.headers["x-daily-requests-left"]?.toIntOrNull() == 0) {
                    logger.info { "No more requests for today!" }

                    false
                } else {
                    httpResponse.status == HttpStatusCode.TooManyRequests
                }
            }
            exponentialDelay()
        }
    }

    init {
        accessId = config.getString("accessId")
        accessSecret = config.getString("accessSecret")
    }

    private suspend fun fetchToken() {
        logger.debug { "Fetching token" }

        val response = httpClient.get(BASE_URL) {
            url.path("/token")
            basicAuth(accessId, accessSecret)
        }.body<CinodeAuthResponse>()

        logger.info { "Got Auth response: $response" }

        authToken = response
    }

    private suspend fun getOrRefreshToken(): CinodeAuthResponse? {
        val decodeJwt = try {
            JWT().decodeJwt(authToken?.accessToken)
        } catch (_: Exception) {
            null
        }

        val now = Instant.now()

        if (decodeJwt == null || now.isAfter(decodeJwt.expiresAt.toInstant())) {
            fetchToken()
        }

        return authToken
    }

    suspend fun getAllUsers(): List<CinodeUser> {
        val token = getOrRefreshToken() ?: return emptyList()

        val response = httpClient.get(BASE_URL) {
            url.path("/v0.1/companies/$companyId/users")
            bearerAuth(token.accessToken)
            header(HttpHeaders.Accept, "application/json")
        }

        logger.info { "Fetched users, got status ${response.status}" }

        return if (response.status == HttpStatusCode.OK) {
            logger.debug { "Got ${response.headers["x-ratelimit-remaining"]} requests remaining towards Cinode before rate limited" }
            response.body()
        } else {
            logger.warn { "Failed to fetch users from Cinode, got status ${response.status}" }
            emptyList()
        }
    }

    suspend fun getSkillsForUser(userId: Int): List<CinodeSkill> {
        val token = getOrRefreshToken() ?: return emptyList()

        logger.debug { "Fetching skills for user with id=$userId" }

        val response = httpClient.get(BASE_URL) {
            url.path("/v0.1/companies/$companyId/users/$userId/skills")
            bearerAuth(token.accessToken)
        }

        val cinodeSkills: List<CinodeSkill> = if (response.status == HttpStatusCode.OK) {
            logger.debug { "Got ${response.headers["x-ratelimit-remaining"]} requests remaining towards Cinode before rate limited" }
            response.body()
        } else {
            val body = response.bodyAsText()
            logger.info { "Failed to fetch cinode skills for user - got ${response.status} with body ['$body']" }
            emptyList()
        }

        return cinodeSkills
    }
}