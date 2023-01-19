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
import se.magello.cinode.CinodeClient
import se.magello.salesforce.SalesForceClient

@Serializable
@Resource("/user/{userId}")
data class User(val userId: Int) {
    @Serializable
    @Resource("/skills")
    data class SkillId(val user: User)
}

@Serializable
@Resource("/users")
data class Users(val limit: Int = 100, val offset: Int = 0)

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

    routing {
        authenticate("azure-jwt") {
            get<User.SkillId> {
                call.respond(cinodeClient.getSkills(it.user.userId))
            }
            get<Users> {
                call.respond(cinodeClient.getUsers(it.limit, it.offset))
            }
            get<SalesForce.Versions> {
                call.respond(salesForceClient.getVersions())
            }
            get<SalesForce.Users> {
                call.respond(salesForceClient.getAllMagelloUsers() ?: Any())
            }
        }
    }
}
