package se.magello.db.tables

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

/**
 * User Table with DAO
 */
object Users : IntIdTable() {
    val email = varchar("email", 1024).index("email_idx")
    val firstName = varchar("firstName", 512)
    val lastName = varchar("lastName", 512)
    val imageUrl = varchar("imageUrl", 2048).nullable()
    val title = varchar("title", 512).nullable()
    val workplace = reference("workplace", Workplaces)
    val preferences = optReference("preferences", UserPreferences)
}
class User(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<User>(Users)
    var email by Users.email
    var firstName by Users.firstName
    var lastName by Users.lastName
    var imageUrl by Users.imageUrl
    var title by Users.title

    val userSkills by UserSkill referrersOn UserSkills.user
    var workplace by Workplace referencedOn Users.workplace
    var preferences by UserPreference optionalReferencedOn Users.preferences
}
