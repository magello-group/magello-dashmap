package se.magello.plugins

import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.typesafe.config.Config
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.response.*
import io.ktor.server.routing.*
import mu.KotlinLogging
import se.magello.cinode.CinodeClient
import se.magello.db.repositories.MapCoordinatesRepository
import se.magello.db.repositories.SkillRepository
import se.magello.db.repositories.UserRepository
import se.magello.db.repositories.WorkAssignmentRepository
import se.magello.map.AddressLookupClient
import se.magello.salesforce.SalesForceClient
import se.magello.workflow.*
import java.io.File

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
    val mapCoordinatesRepository = MapCoordinatesRepository()

    val staticFileDirPath = config.getConfig("frontend").getString("staticFilePath")
    val routes = routing {
        singlePageApplication {
            react(staticFileDirPath)
        }

        get<Api.Workplaces.Workplace> {
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
        get<Api.Workplaces> {
            try {
                val message = workAssignmentRepository.getAllWorkAssignments(it.limit, it.offset)
                call.respond(message)
            } catch (je: JobRunningException) {
                call.respond(HttpStatusCode.ServiceUnavailable)
            }
        }
        authenticate("azure-jwt") {
            // Users
            get<Api.Users> {
                try {
                    val allUsers = userRepository.getAllUsers(it.limit, it.offset)
                    call.respond(allUsers)
                } catch (je: JobRunningException) {
                    // TODO: Make it so we don't have to copy this
                    call.respond(HttpStatusCode.ServiceUnavailable)
                }
            }
            get<Api.Users.Id> {
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
            get<Api.Users.Self> {
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
            post<Api.Users.Self.Preferences> {
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
            // Admin
            get<Api.Admin.ExportFoodPreferences> {
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
            get<Api.Admin.Coordinates.Unmapped> {
                call.principal<JWTPrincipal>()?.let { jwt ->
                    if (jwt.isAdmin()) {
                        call.respond(mapCoordinatesRepository.getAllUnmappedWorkplaces(it.limit, it.offset))
                    } else {
                        call.respond(HttpStatusCode.Unauthorized)
                    }
                } ?: call.respond(HttpStatusCode.Unauthorized)
            }
            put<Api.Admin.Coordinates.Organisation.Edit> {
                call.principal<JWTPrincipal>()?.let { jwt ->
                    if (jwt.isAdmin()) {
                        mapCoordinatesRepository.setCoordinatesForWorkplace(it.parent.id, it.longitude, it.latitude)
                        call.respond(HttpStatusCode.NoContent)
                    } else {
                        call.respond(HttpStatusCode.Unauthorized)
                    }
                } ?: call.respond(HttpStatusCode.Unauthorized)
            }
            // Skills
            get<Api.Skill.Search> {
                call.respond(skillRepository.searchSkill(it.query))
            }
            get<Api.Skill.Id> {
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
