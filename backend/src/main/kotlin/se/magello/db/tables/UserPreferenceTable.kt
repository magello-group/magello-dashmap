package se.magello.db.tables

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable

/**
 * User Preferences table with DAO
 */
object UserPreferences : IdTable<String>() {
    override val id = varchar("id", 1024).entityId().uniqueIndex()
    val dietPreferences = text("dietPreferences", eagerLoading = true)
    val extraDietPreferences = text("extraDietPreferences", eagerLoading = true).nullable()
    val socials = text("socials", eagerLoading = true)
    val quote = varchar("quote", 2048).nullable()
}
class UserPreference(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, UserPreference>(UserPreferences)
    var dietPreferences by UserPreferences.dietPreferences
    var extraDietPreferences by UserPreferences.extraDietPreferences
    var socials by UserPreferences.socials
    var quote by UserPreferences.quote

    val user by User optionalReferrersOn Users.preferences
}

