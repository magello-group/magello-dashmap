package se.magello.plugins

import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.typesafe.config.Config
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.HttpMethodRouteSelector
import io.ktor.server.routing.Route
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable
import mu.KotlinLogging
import se.magello.cinode.CinodeClient
import se.magello.db.repositories.SkillRepository
import se.magello.db.repositories.UserRepository
import se.magello.db.repositories.WorkAssignmentRepository
import se.magello.map.AddressLookupClient
import se.magello.salesforce.SalesForceClient
import se.magello.workflow.*


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

    @Serializable
    @Resource("/{userId}")
    data class Id(val parent: Users = Users(), val userId: Int)

    @Serializable
    @Resource("foodpreferences/export")
    data class ExportFoodPreferences(val parent: Users = Users())
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

// TODO: clients out and add them to workflow setup
//   - Create wrapper object for responses: {data: T, errors: []Errors}
//   - Handle JobIsRunning exceptions in all repositories.
//   - Create an internal and external model
fun Application.configureRouting(config: Config) {
    val cinodeConfig = config.getConfig("cinode")
    val salesForceConfig = config.getConfig("salesforce")
    val cinodeClient = CinodeClient(cinodeConfig)
    val salesForceClient = SalesForceClient(salesForceConfig)
    val workflow = MergeUserDataWorkflow(UserDataFetcher(cinodeClient, salesForceClient, AddressLookupClient()))

    val userRepository = UserRepository(workflow)
    val skillRepository = SkillRepository()
    val workAssignmentRepository = WorkAssignmentRepository(workflow)

    val routes = routing {
        get<Workplaces.Workplace> {
            try {
                val message = workAssignmentRepository.getWorkAssignment(it.organisationId)
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
                val message = workAssignmentRepository.getAllWorkAssignments(it.limit, it.offset)
                call.respond(message)
            } catch (je: JobRunningException) {
                call.respond(HttpStatusCode.ServiceUnavailable)
            }
        }
        authenticate("azure-jwt") {
            get<Users> {
                try {
                    val allUsers = userRepository.getAllUsers(it.limit, it.offset)
                    call.respond(allUsers)
                } catch (je: JobRunningException) {
                    // TODO: Make it so we don't have to copy this
                    call.respond(HttpStatusCode.ServiceUnavailable)
                }
            }
            get<Users.Id> {
                try {
                    userRepository.getUser(it.userId)?.let { user ->
                        call.respond(user)
                    } ?: run {
                        call.respond(HttpStatusCode.NotFound)
                    }
                } catch (je: JobRunningException) {
                    // TODO: Make it so we don't have to copy this
                    call.respond(HttpStatusCode.ServiceUnavailable)
                }
            }
            get<Users.Self> {
                when (val principal = call.principal<JWTPrincipal>()) {
                    null -> call.respond(HttpStatusCode.Unauthorized)

                    else -> {
                        when (val userSelf = userRepository.getUserSelf(principal)) {
                            null -> call.respond(HttpStatusCode.NotFound)
                            else -> call.respond(userSelf)
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
                            userRepository.postUserPreferences(principal, preferences)
                            call.respond(HttpStatusCode.NoContent)
                        } catch (ie: IllegalStateException) {
                            logger.info { ie.message }
                            call.respond(HttpStatusCode.BadRequest)
                        }
                    }
                }
            }
            get<Users.ExportFoodPreferences> {
                call.principal<JWTPrincipal>()?.let {
                    if (it.isAdmin()) {
                        val rows = userRepository.getAllUserPreferences().csvFormat()
                        call.response.headers.append(
                            HttpHeaders.ContentDisposition,
                            ContentDisposition.Attachment.disposition
                        )
                        call.respondOutputStream(ContentType.Text.CSV) {
                            csvWriter().writeAll(rows, this)
                        }
                    } else {
                        call.respond(HttpStatusCode.Unauthorized)
                    }
                } ?: call.respond(HttpStatusCode.Unauthorized)
            }
            get<Skill.Search> {
                call.respond(skillRepository.searchSkill(it.query))
            }
            get<Skill.Id> {
                call.respond(skillRepository.getUserSkillsForSkillId(it.id))
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
