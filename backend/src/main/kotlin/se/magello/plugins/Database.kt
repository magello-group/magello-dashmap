package se.magello.plugins

import com.typesafe.config.Config
import io.ktor.server.application.Application
import io.ktor.server.config.tryGetString
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import se.magello.db.tables.LatestRefresh
import se.magello.db.tables.Skills
import se.magello.db.tables.UserPreferences
import se.magello.db.tables.UserSkills
import se.magello.db.tables.Users
import se.magello.db.tables.Workplaces

fun Application.configureDatabase(config: Config) {
    val url = config.getString("url")
    val username = config.getString("username")
    val password = config.tryGetString("password") ?: ""

    Database.connect(url, user = username, password = password)

    transaction {
        SchemaUtils.create(Users, Skills, Workplaces, UserSkills, LatestRefresh, UserPreferences)
    }
}
