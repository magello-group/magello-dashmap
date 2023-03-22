package se.magello.map

import kotlinx.serialization.Serializable
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Client for looking up addresses from Organisation ids.
 */
class AddressLookupClient {
    private val addressLookupCache = mutableMapOf(
        "556531-7129" to MAGELLO_OFFICE_COORDINATES,
        // ICA Sverige AB
        "556021-0261" to Coordinates(lat = 59.371593671766185, lon = 18.008744098475763, type = "map"),
        // KG Knutsson AB
        "556075-1975" to Coordinates(lon = 17.95093, lat = 59.42804, type = "map"),
        "202100-4185" to Coordinates(lat = 59.3607322, lon = 17.9762169, type = "map"),
        // EKN Exportkreditnämnden
        "202100-2098" to Coordinates(lat = 59.335984773054776, lon = 18.064189098474348, type = "map"),
        // Skatteverket
        "202100-5448" to Coordinates(lat = 59.36135983481525, lon = 17.975169884981213, type = "map"),
        // Trafikförvaltningen
        "556013-0683" to Coordinates(lat = 59.33607420906211, lon = 18.01462894975202, type = "map"),
        // Alecta
        "502014-6865" to Coordinates(lat = 59.339604193238124, lon = 18.065994856145032, type = "map"),
        // Polismyndigheten
        "202100-0076" to Coordinates(lat = 59.33141055880402, lon = 18.03772050032125, type = "map"),
        // Svenska Kraftnät
        "202100-4284" to Coordinates(lat = 59.36089176858232, lon = 17.97436082731063, type = "map"),
        // Vattenfall AB
        "556036-2138" to Coordinates(lat = 59.369302874774704, lon = 18.00280919847565, type = "map"),
        // KTH
        "202100-3054" to Coordinates(lat = 59.34992340215211, lon = 18.070489540804303, type = "map"),
        // Martin och Servera
        "556233-2451" to Coordinates(lat = 59.294859227377195, lon = 18.036790884978533, type = "map"),
        // Arbetsförmedlingen
        "202100-2114" to Coordinates(lat = 59.356494850489355, lon = 17.978591927310426, type = "map"),
    )

    suspend fun lookupOrganisationPosition(organisationNumber: String): Coordinates {
        val coordinates = addressLookupCache[organisationNumber]
        if (coordinates != null) {
            return coordinates
        }

        logger.info { "No coordinates set up for [organisationNumber=$organisationNumber]" }
        // TODO: Return something else than Magello coordinates, maybe a sealed class were we have "Unmapped" as a type.
        return MAGELLO_OFFICE_COORDINATES
    }

    companion object {
        val MAGELLO_OFFICE_COORDINATES = Coordinates(18.0537319, 59.3302155, "map")
    }
}

@Serializable
data class Coordinates(val lon: Double, val lat: Double, val type: String)
