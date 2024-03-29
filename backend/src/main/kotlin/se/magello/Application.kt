package se.magello

import com.typesafe.config.ConfigFactory
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.request.path
import io.ktor.server.resources.Resources
import io.ktor.server.routing.Routing
import org.slf4j.event.Level
import se.magello.plugins.configureDatabase
import se.magello.plugins.configureRouting
import se.magello.plugins.configureSecurity

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    val applicationConfig = ConfigFactory.defaultApplication()

    configureDatabase(applicationConfig.getConfig("database").resolve())

    install(ContentNegotiation) {
        json()
    }
    install(Resources)
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }
    install(CORS) {
        val corsConfig = applicationConfig.getConfig("routing.cors")

        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Post)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)

        allowOrigins {
            it.endsWith("magello-dashmap-backend.nicefield-05120e49.swedencentral.azurecontainerapps.io")
        }

        allowSameOrigin = true
        allowCredentials = true
    }

    configureSecurity(applicationConfig.getConfig("security"))
    configureRouting(applicationConfig.getConfig("routing"))
}
