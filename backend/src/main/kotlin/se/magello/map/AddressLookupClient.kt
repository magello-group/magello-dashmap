package se.magello.map

import mu.KotlinLogging
import se.magello.workflow.MagelloCoordinates

private val logger = KotlinLogging.logger {}

/**
 * Client for looking up addresses from Organisation ids.
 */
class AddressLookupClient {
    private val addressLookupCache = mutableMapOf(
        "556531-7129" to MAGELLO_OFFICE_COORDINATES,
        // ICA Sverige AB
        "556021-0261" to MagelloCoordinates.Mapped(lat = 59.371593671766185, lon = 18.008744098475763),
        // KG Knutsson AB
        "556075-1975" to MagelloCoordinates.Mapped(lon = 17.95093, lat = 59.42804),
        "202100-4185" to MagelloCoordinates.Mapped(lat = 59.3607322, lon = 17.9762169),
        // EKN Exportkreditnämnden
        "202100-2098" to MagelloCoordinates.Mapped(lat = 59.335984773054776, lon = 18.064189098474348),
        // Skatteverket
        "202100-5448" to MagelloCoordinates.Mapped(lat = 59.36135983481525, lon = 17.975169884981213),
        // Trafikförvaltningen
        "556013-0683" to MagelloCoordinates.Mapped(lat = 59.33607420906211, lon = 18.01462894975202),
        // Alecta
        "502014-6865" to MagelloCoordinates.Mapped(lat = 59.339604193238124, lon = 18.065994856145032),
        // Polismyndigheten
        "202100-0076" to MagelloCoordinates.Mapped(lat = 59.33141055880402, lon = 18.03772050032125),
        // Svenska Kraftnät
        "202100-4284" to MagelloCoordinates.Mapped(lat = 59.36089176858232, lon = 17.97436082731063),
        // Vattenfall AB
        "556036-2138" to MagelloCoordinates.Mapped(lat = 59.369302874774704, lon = 18.00280919847565),
        // KTH
        "202100-3054" to MagelloCoordinates.Mapped(lat = 59.34992340215211, lon = 18.070489540804303),
        // Martin och Servera
        "556233-2451" to MagelloCoordinates.Mapped(lat = 59.294859227377195, lon = 18.036790884978533),
        // Arbetsförmedlingen
        "202100-2114" to MagelloCoordinates.Mapped(lat = 59.356494850489355, lon = 17.978591927310426),
    )

    fun lookupOrganisationPosition(organisationNumber: String): MagelloCoordinates {
        val coordinates = addressLookupCache[organisationNumber]
        if (coordinates != null) {
            return coordinates
        }

        logger.info { "No coordinates set up for [organisationNumber=$organisationNumber]" }
        return MagelloCoordinates.Unmapped
    }

    companion object {
        val MAGELLO_OFFICE_COORDINATES = MagelloCoordinates.Mapped(18.0537319, 59.3302155)
    }
}
