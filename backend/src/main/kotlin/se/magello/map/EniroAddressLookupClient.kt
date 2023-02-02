package se.magello.map

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.resources.Resources
import io.ktor.client.plugins.resources.get
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Client for looking up addresses from Organisation ids. This is using Eniro to lookup addresses, which is not the best
 * source.
 */
class EniroAddressLookupClient {
    // TODO: Map manually for now, Eniro is not the best source...
    private val addressLookupCache = mutableMapOf(
        "556531-7129" to MAGELLO_OFFICE_COORDINATES,
        "556021-0261" to Coordinates(18.734884, 49.214957, "map"),
        "556075-1975" to Coordinates(17.95093, 59.42804, "map")
    )

    private val httpClient: HttpClient = HttpClient(OkHttp) {
        install(Resources)
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
        defaultRequest {
            host = "www.eniro.se"
            port = 443
            url { protocol = URLProtocol.HTTPS }
        }
    }

    suspend fun lookupOrganisationPosition(organisationNumber: String): Coordinates {
        val coordinates = addressLookupCache[organisationNumber]
        if (coordinates != null) {
            return coordinates
        }

        val response = httpClient.get(EniroRequest.CompanySearch(organisationNumber = organisationNumber))

        if (response.status == HttpStatusCode.OK) {
            val eniroRequest = response.body<EniroPageState>()
            val firstCoordinates = eniroRequest.companies.flatMap { company ->
                company.addresses.mapNotNull { it.coordinates.firstOrNull() }
            }
            val companyCoordinates = firstCoordinates.firstOrNull()

            return if (companyCoordinates == null) {
                logger.warn { "Failed to find organisation with number $organisationNumber on Eniro, putting user in Magello" }

                MAGELLO_OFFICE_COORDINATES
            } else {
                addressLookupCache.putIfAbsent(organisationNumber, companyCoordinates)

                logger.info { "Found coordinates for $organisationNumber!" }

                companyCoordinates
            }
        } else {
            logger.warn { "Failed request to find organisation with number $organisationNumber on Eniro, putting user in Magello" }
            return MAGELLO_OFFICE_COORDINATES
        }
    }

    companion object {
        val MAGELLO_OFFICE_COORDINATES = Coordinates(18.0537319, 59.3302155, "map")
    }
}