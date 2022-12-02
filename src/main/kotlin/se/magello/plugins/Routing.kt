package se.magello.plugins

import com.typesafe.config.Config
import io.ktor.resources.Resource
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.resources.get
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable
import se.magello.CinodeClient

@Serializable
@Resource("/user/{userId}")
data class User(val userId: Int) {    @Serializable
    @Resource("/skills")
    data class SkillId(val user: User)
}

@Serializable
@Resource("/users")
data class Users(val limit: Int = 100, val offset: Int = 0)

fun Application.configureRouting(config: Config) {
    val cinodeConfig = config.getConfig("cinode")
    val cinodeClient = CinodeClient(cinodeConfig)

    routing {
        authenticate("azure-jwt") {
            get<User.SkillId> {
                call.respond(cinodeClient.getSkills(it.user.userId))
            }
            get<Users> {
                call.respond(cinodeClient.getUsers(it.limit, it.offset))
            }
        }
    }
}

data class UserSession(val token: String)