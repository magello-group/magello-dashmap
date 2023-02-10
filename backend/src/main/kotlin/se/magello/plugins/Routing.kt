package se.magello.plugins

import com.typesafe.config.Config
import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.resources.get
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable
import se.magello.cinode.CinodeClient
import se.magello.map.EniroAddressLookupClient
import se.magello.salesforce.SalesForceClient
import se.magello.workflow.UserDataFetcher
import se.magello.workflow.MergeUserDataWorkflow
import se.magello.workflow.JobRunningException


@Serializable
@Resource("/workplaces")
data class Workplaces(val limit: Int = 10, val offset: Int = 0) {
    @Serializable
    @Resource("/workplaces/{organisationId}")
    data class Workplace(val parent: Workplaces = Workplaces(), val organisationId: String)
}

@Serializable
@Resource("/users")
data class Users(val limit: Int = 10, val offset: Int = 0)

@Serializable
@Resource("/salesforce")
class SalesForce {
    @Serializable
    @Resource("/versions")
    data class Versions(val parent: SalesForce = SalesForce(), val limit: Int = 100, val offset: Int = 0)

    @Serializable
    @Resource("/users")
    data class Users(val parent: SalesForce = SalesForce())
}

fun Application.configureRouting(config: Config) {
    val cinodeConfig = config.getConfig("cinode")
    val salesForceConfig = config.getConfig("salesforce")
    val cinodeClient = CinodeClient(cinodeConfig)
    val salesForceClient = SalesForceClient(salesForceConfig)
    val client = MergeUserDataWorkflow(UserDataFetcher(cinodeClient, salesForceClient, EniroAddressLookupClient()))

    routing {
        get<Workplaces.Workplace> {
            try {
                val message = client.getWorkAssignment(it.organisationId)
                if (message == null) {
                    call.respond(HttpStatusCode.NotFound)
                } else {
                    call.respond(message)
                }
            } catch (je: JobRunningException) {
                call.respond(HttpStatusCode.ServiceUnavailable)
            }
        }
        get<Workplaces> {
            try {
                val message = client.getAllWorkAssignments(it.limit, it.offset)
                call.respond(message)
            } catch (je: JobRunningException) {
                call.respond(HttpStatusCode.ServiceUnavailable)
            }
        }
        get<Users> {
            try {
                val message = client.getAllUsers(it.limit, it.offset)
                call.respond(message)
            } catch (je: JobRunningException) {
                // TODO: Make it so we don't have to copy this
                call.respond(HttpStatusCode.ServiceUnavailable)
            }
        }
        authenticate("azure-jwt") {
            get<SalesForce.Versions> {
                call.respond(salesForceClient.getVersions())
            }
            get<SalesForce.Users> {
                call.respond(salesForceClient.getAllMagelloUsers() ?: Any())
            }
        }
    }
}
