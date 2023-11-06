package se.magello.plugins

import com.typesafe.config.Config
import io.ktor.server.config.tryGetString
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import se.magello.db.tables.*
import se.magello.db.tables.Users
import se.magello.db.tables.Workplaces
import se.magello.map.AddressLookupClient.Companion.MAGELLO_OFFICE_COORDINATES
import se.magello.workflow.MagelloCoordinates
import se.magello.workflow.MagelloWorkAssignment

// Some hardcoded values - perfect!
private val initialCoordinates = listOf(
    MagelloWorkAssignment(
        organisationId = "556531-7129",
        companyName = "Magello",
        coordinates = MAGELLO_OFFICE_COORDINATES,
    ),
    /*
    MagelloWorkAssignment(
        "556021-0261",
        "ICA Sverige AB",
        MagelloCoordinates.Mapped(lat = 59.371593671766185, lon = 18.008744098475763),
    ),
     */
    MagelloWorkAssignment(
        "556075-1975",
        "KG Knutsson AB",
        MagelloCoordinates.Mapped(lon = 17.95093, lat = 59.42804),
    ),
    MagelloWorkAssignment(
        "202100-4185",
        "Skolverket",
        MagelloCoordinates.Mapped(lat = 59.3607322, lon = 17.9762169),
    ),
    MagelloWorkAssignment(
        "202100-2098",
        "EKN Exportkreditnämnden",
        MagelloCoordinates.Mapped(lat = 59.335984773054776, lon = 18.064189098474348),
    ),
    MagelloWorkAssignment(
        "202100-5448",
        "Skatteverket",
        MagelloCoordinates.Mapped(lat = 59.36135983481525, lon = 17.975169884981213),
    ),
    MagelloWorkAssignment(
        "556013-0683",
        "Trafikförvaltningen",
        MagelloCoordinates.Mapped(lat = 59.33607420906211, lon = 18.01462894975202),
    ),
    MagelloWorkAssignment(
        "502014-6865",
        "Alecta",
        MagelloCoordinates.Mapped(lat = 59.339604193238124, lon = 18.065994856145032),
    ),
    MagelloWorkAssignment(
        "202100-0076",
        "Polismyndigheten",
        MagelloCoordinates.Mapped(lat = 59.33141055880402, lon = 18.03772050032125),
    ),
    MagelloWorkAssignment(
        "202100-4284",
        "Svenska Kraftnät",
        MagelloCoordinates.Mapped(lat = 59.36089176858232, lon = 17.97436082731063),
    ),
    MagelloWorkAssignment(
        "556036-2138",
        "Vattenfall AB",
        MagelloCoordinates.Mapped(lat = 59.369302874774704, lon = 18.00280919847565),
    ),
    MagelloWorkAssignment(
        "202100-3054",
        "KTH",
        MagelloCoordinates.Mapped(lat = 59.34992340215211, lon = 18.070489540804303),
    ),
    MagelloWorkAssignment(
        "556233-2451",
        "Martin och Servera",
        MagelloCoordinates.Mapped(lat = 59.294859227377195, lon = 18.036790884978533),
    ),
    MagelloWorkAssignment(
        "202100-2114",
        "Arbetsförmedlingen",
        MagelloCoordinates.Mapped(lat = 59.356494850489355, lon = 17.978591927310426),
    ),
    MagelloWorkAssignment(
        "556311-9204",
        "Wasa Kredit AB",
        MagelloCoordinates.Mapped(lat = 59.349385279294005, lon = 18.1021045503781)
    ),
    MagelloWorkAssignment(
        "202100-5224",
        "Svenska ESF-Rådet",
        MagelloCoordinates.Mapped(lat = 59.31420699503986, lon = 18.07115717245701)
    ),
    /*
    MagelloWorkAssignment(
        "556703-1702",
        "Scandic Hotels",
        MagelloCoordinates.Mapped(lat = 59.350624193685064, lon = 18.04618044165233)
    )
     */
)

fun configureDatabase(config: Config) {
    val url = config.getString("url")
    val username = config.getString("username")
    val password = config.tryGetString("password") ?: ""

    Database.connect(url, user = username, password = password)

    transaction {
        SchemaUtils.create(
            Users,
            Skills,
            Workplaces,
            UserSkills,
            LatestRefresh,
            UserPreferences,
            MappedCoordinatesTable
        )

        initialCoordinates.forEach {
            MappedCoordinates.findById(it.organisationId) ?: MappedCoordinates.new(it.organisationId) {
                companyName = it.companyName
                longitude = (it.coordinates as MagelloCoordinates.Mapped).lon
                latitude = it.coordinates.lat
            }
        }
    }
}
