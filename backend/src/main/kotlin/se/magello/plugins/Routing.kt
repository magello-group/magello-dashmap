package se.magello.plugins

import com.typesafe.config.Config
import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.routing.HttpMethodRouteSelector
import io.ktor.server.routing.Route
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable
import mu.KotlinLogging
import se.magello.cinode.CinodeClient
import se.magello.map.EniroAddressLookupClient
import se.magello.salesforce.SalesForceClient
import se.magello.workflow.UserDataFetcher
import se.magello.workflow.MergeUserDataWorkflow
import se.magello.workflow.JobRunningException
import se.magello.workflow.MagelloUserPreferences


@Serializable
@Resource("/workplaces")
data class Workplaces(val limit: Int = 10, val offset: Int = 0) {
    @Serializable
    @Resource("/{organisationId}")
    data class Workplace(val parent: Workplaces = Workplaces(), val organisationId: String)
}

@Serializable
@Resource("/users")
data class Users(val limit: Int = 10, val offset: Int = 0) {
    @Serializable
    @Resource("self")
    data class Self(val parent: Users = Users()) {
        @Serializable
        @Resource("preferences")
        data class Preferences(val parent: Self = Self())
    }
}

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

@Serializable
@Resource("/skill")
class Skill {
    @Serializable
    @Resource("/search")
    data class Search(val parent: Skill = Skill(), val query: String)

    @Serializable
    @Resource("/{id}")
    data class Id(val parent: Skill = Skill(), val id: Int)
}

private val logger = KotlinLogging.logger {}
fun Application.configureRouting(config: Config) {
    val cinodeConfig = config.getConfig("cinode")
    val salesForceConfig = config.getConfig("salesforce")
    val cinodeClient = CinodeClient(cinodeConfig)
    val salesForceClient = SalesForceClient(salesForceConfig)
    val client = MergeUserDataWorkflow(UserDataFetcher(cinodeClient, salesForceClient, EniroAddressLookupClient()))

    val routes = routing {
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
            get<Users.Self> {
                when (val principal = call.principal<JWTPrincipal>()) {
                    null -> call.respond(HttpStatusCode.Unauthorized)
                    else -> {
                        when (val message = client.getUserSelf(principal)) {
                            null -> call.respond(HttpStatusCode.NotFound) // TODO: Return a better response explaining why we cant find the user
                            else -> call.respond(message)
                        }
                    }
                }
            }
            post<Users.Self.Preferences> {
                when (val principal = call.principal<JWTPrincipal>()) {
                    null -> call.respond(HttpStatusCode.Unauthorized)
                    else -> {
                        val preferences = call.receive<MagelloUserPreferences>()
                        try {
                            client.postUserPreferences(principal, preferences)
                            call.respond(HttpStatusCode.NoContent)
                        } catch (ie: IllegalStateException) {
                            logger.info { ie.message }
                            call.respond(HttpStatusCode.BadRequest)
                        }
                    }
                }
            }
            get<Skill.Search> {
                call.respond(client.searchSkill(it.query))
            }
            get<Skill.Id> {
                call.respond(client.getUserSkillsForSkillId(it.id))
            }
            get<SalesForce.Versions> {
                call.respond(salesForceClient.getVersions())
            }
            get<SalesForce.Users> {
                call.respond(salesForceClient.getAllMagelloUsers() ?: Any())
            }
        }
    }

    val allRoutes = allRoutes(routes)
    val allRoutesWithMethod = allRoutes.filter { it.selector is HttpMethodRouteSelector }
    allRoutesWithMethod.forEach {
        logger.info("route: $it")
    }
}

fun allRoutes(root: Route): List<Route> {
    return listOf(root) + root.children.flatMap { allRoutes(it) }
}
