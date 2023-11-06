package se.magello.db.repositories

import kotlinx.serialization.Serializable
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.transaction
import se.magello.db.tables.MappedCoordinates
import se.magello.workflow.MagelloCoordinates
import se.magello.workflow.fromPoints

@Serializable
data class UnmappedWorkplace(val companyName: String, val organisationId: String)

private val logger = KotlinLogging.logger {}

class MapCoordinatesRepository {
    fun getAllUnmappedWorkplaces(limit: Int, offset: Long): List<UnmappedWorkplace> {
        return transaction {
            MappedCoordinates
                .all()
                .limit(limit, offset)
                .mapNotNull {
                    when (fromPoints(it.longitude, it.latitude)) {
                        is MagelloCoordinates.Unmapped -> UnmappedWorkplace(it.companyName, it.id.value)

                        is MagelloCoordinates.Mapped -> null
                    }
                }
        }
    }

    fun setCoordinatesForWorkplace(organisationId: String, longitude: Double, latitude: Double) {
        transaction {
            MappedCoordinates.findById(organisationId)?.let {
                it.apply {
                    this.longitude = longitude
                    this.latitude = latitude
                }
            } ?: run { logger.warn { "Failed to find unmapped coordinates for [organisationId=$organisationId]" } }
        }
    }
}