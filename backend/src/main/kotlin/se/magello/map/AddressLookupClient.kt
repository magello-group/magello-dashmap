package se.magello.map

import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.transaction
import se.magello.db.tables.MappedCoordinates
import se.magello.workflow.MagelloCoordinates
import se.magello.workflow.fromPoints

private val logger = KotlinLogging.logger {}

/**
 * Client for looking up addresses from Organisation ids.
 */
class AddressLookupClient {
    fun lookupOrganisationPosition(organisationNumber: String): MagelloCoordinates {
        val mappedCoordinates = transaction {
            MappedCoordinates.findById(organisationNumber)
        }

        val coordinates = fromPoints(mappedCoordinates?.longitude, mappedCoordinates?.latitude)
        if (coordinates is MagelloCoordinates.Unmapped) {
            logger.info { "No coordinates set up for [organisationNumber=$organisationNumber]" }
        }

        return coordinates
    }

    companion object {
        val MAGELLO_OFFICE_COORDINATES = MagelloCoordinates.Mapped(18.0537319, 59.3302155)
    }
}
