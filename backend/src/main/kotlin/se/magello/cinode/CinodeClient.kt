package se.magello.cinode

import com.auth0.jwt.JWT
import com.sksamuel.aedile.core.caffeineBuilder
import com.typesafe.config.Config
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.basicAuth
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.path
import io.ktor.serialization.kotlinx.json.json
import java.time.Duration
import java.time.Instant
import java.util.Timer
import kotlin.concurrent.timer
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

private var usersFetched = Instant.EPOCH
private const val BASE_URL = "https://api.cinode.com"

class CinodeClient(
    config: Config,
) {
    private val accessSecret: String
    private val accessId: String
    private val t: Timer

    private val companyId = 150

    private var authToken: CinodeAuthResponse? = null

    private val skillCache = caffeineBuilder<Int, List<CinodeSkill>> {}
        .build { getSkillsForUser(it) }

    private var userCache = emptyMap<Int, CinodeUser>()

    private val httpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    init {
        accessId = config.getString("accessId")
        accessSecret = config.getString("accessSecret")

        t = timer(period = Duration.ofMinutes(10).toMillis()) {
            runBlocking {
                if (Duration.between(usersFetched, Instant.now()) > Duration.ofDays(1)) {
                    usersFetched = try {
                        getAllUsers()
                        Instant.now()
                    } catch (e: Exception) {
                        logger.error(e) { "Failed to fetch users" }
                        Instant.EPOCH
                    }
                }
            }
        }
    }

    suspend fun getSkills(id: Int): List<CinodeSkill> {
        return skillCache.get(id)
    }

    fun getUsers(limit: Int, offset: Int): List<CinodeUser> {
        return userCache
            .asSequence()
            .drop(offset)
            .take(limit)
            .map { it.value }
            .toList()
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

    private suspend fun getAllUsers() {
        val token = getOrRefreshToken() ?: return

        val response = httpClient.get(BASE_URL) {
            url.path("/v0.1/companies/$companyId/users")
            bearerAuth(token.accessToken)
            header(HttpHeaders.Accept, "application/json")
        }

        logger.info { "Fetched users, got status ${response.status}" }

        if (response.status == HttpStatusCode.OK) {
            val users = response.body<List<CinodeUser>>()

            userCache = users.associateBy { it.companyUserId }
        }
    }

    private suspend fun getSkillsForUser(userId: Int): List<CinodeSkill> {
        val token = getOrRefreshToken() ?: return emptyList()

        logger.info { "Fetching skills for user with id=$userId" }

        val response = httpClient.get(BASE_URL) {
            url.path("/v0.1/companies/$companyId/users/$userId/skills")
            bearerAuth(token.accessToken)
        }

        val cinodeSkills: List<CinodeSkill> = if (response.status == HttpStatusCode.OK) {
            response.body()
        } else {
            val body = response.bodyAsText()
            logger.info { "Failed to fetch cinode skills for user - got ${response.status} with body ['$body']" }
            emptyList()
        }

        return cinodeSkills
    }
}