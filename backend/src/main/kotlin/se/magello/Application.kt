package se.magello

import com.typesafe.config.ConfigFactory
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.resources.Resources
import org.slf4j.event.*
import se.magello.plugins.configureRouting
import se.magello.plugins.configureSecurity

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused")
fun Application.module() {
    val applicationConfig = ConfigFactory.defaultApplication()

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
        allowHeader(HttpHeaders.Authorization)

        corsConfig.getStringList("allowedHosts").forEach {
            allowHost(it)
        }

        allowCredentials = true
    }

    configureSecurity(applicationConfig.getConfig("security"))
    configureRouting(applicationConfig.getConfig("routing"))
}
