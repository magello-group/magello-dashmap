package se.magello.plugins

import com.auth0.jwk.JwkProviderBuilder
import com.typesafe.config.Config
import io.ktor.server.application.Application
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTCredential
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import java.net.URI
import java.net.URL
import java.util.concurrent.TimeUnit

fun Application.configureSecurity(config: Config) {
    val jwtConfig = config.getConfig("jwt")

    authentication {
        val issuer = jwtConfig.getString("issuer")
        val jwksUrl = jwtConfig.getString("jwksUrl")
        val requiredGroups = jwtConfig.getStringList("requiredGroups")

        val provider = JwkProviderBuilder(URI(jwksUrl).toURL())
            .cached(10, 24, TimeUnit.HOURS)
            .rateLimited(10, 1, TimeUnit.MINUTES)
            .build()

        jwt("azure-jwt") {
            verifier(provider, issuer) {
                // acct 0 means the user is part of this tenant and not a guest (1)
                withClaim("acct", 0)
            }
            validate {
                val groups = it.getListClaimOrEmpty("groups")
                if (groups.containsAll(requiredGroups)) {
                    JWTPrincipal(it.payload)
                } else {
                    null
                }
            }
        }
    }
}

private fun JWTCredential.getListClaimOrEmpty(name: String): List<String> = try {
    this.getListClaim(name, String::class)
} catch (e: NullPointerException) {
    emptyList()
}

// Ground control
private const val ADMIN_GROUP = "7941ea19-db89-4329-bf4b-f2c51dd32618"
private val extraAdmins = listOf("fabian.eriksson@magello.se")
fun JWTPrincipal.isAdmin(): Boolean = try {
    val groups = getListClaim("groups", String::class)
    val email = getClaim("email", String::class)

    // If user is in the Admin group or is one of the extra admins
    groups.contains(ADMIN_GROUP) || (!email.isNullOrBlank() && extraAdmins.contains(email))
} catch (e: NullPointerException) {
    false
}
