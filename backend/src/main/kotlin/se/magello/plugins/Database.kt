package se.magello.plugins

import com.typesafe.config.Config
import io.ktor.server.application.Application
import io.ktor.server.config.tryGetString
import java.time.Instant
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.transactions.transaction
import se.magello.db.LatestRefresh
import se.magello.db.Refresh
import se.magello.db.Skills
import se.magello.db.UserSkills
import se.magello.db.Users
import se.magello.db.Workplaces

fun Application.configureDatabase(config: Config) {
    val url = config.getString("url")
    val username = config.getString("username")
    val password = config.tryGetString("password") ?: ""

    Database.connect(url, user = username, password = password)

    transaction {
        SchemaUtils.create(Users, Skills, Workplaces, UserSkills, LatestRefresh)
    }
}
