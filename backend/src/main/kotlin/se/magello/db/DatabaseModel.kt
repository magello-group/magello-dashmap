package se.magello.db

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

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

/**
 * User Preferences table with DAO
 */
object UserPreferences : IdTable<String>() {
    override val id = varchar("id", 1024).entityId()
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

/**
 * Skills Table with DAO
 */
object Skills : IntIdTable() {
    val masterSynonym = varchar("masterSynonym", 1024).index()
    val synonyms = text("synonyms").nullable()
}
class Skill(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Skill>(Skills)
    var masterSynonym by Skills.masterSynonym
    var synonyms by Skills.synonyms

    val userSkills by UserSkill referrersOn UserSkills.skill
}

/**
 * UserSkills, maps the user to the skill and the users' score of that skill
 */
object UserSkills : IdTable<String>() {
    override val id = varchar("id", 1024).entityId()

    val favourite = bool("favourite")
    val level = integer("level").nullable()
    val levelGoal = integer("levelGoal").nullable()
    val levelGoalDeadline = varchar("levelGoalDeadline", 1024).nullable()
    val numberOfDaysWorkExperience = integer("numberOfDaysWorkExperience").nullable()

    val user = reference("user", Users)
    val skill = reference("skill", Skills)
}
class UserSkill(id: EntityID<String>) : Entity<String>(id) {
    companion object: EntityClass<String, UserSkill>(UserSkills)
    var favourite by UserSkills.favourite
    var level by UserSkills.level
    var levelGoal by UserSkills.levelGoal
    var levelGoalDeadline by UserSkills.levelGoalDeadline
    var numberOfDaysWorkExperience by UserSkills.numberOfDaysWorkExperience

    var user by User referencedOn UserSkills.user
    var skill by Skill referencedOn UserSkills.skill
}

/**
 * Workplaces table with DAO
 */
object Workplaces : IdTable<String>() {
    override val id = varchar("id", 25).entityId()
    val companyName = varchar("companyName", 1024).index()
    val longitude = double("longitude")
    val latitude = double("latitude")
}
class Workplace(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, Workplace>(Workplaces)
    var companyName by Workplaces.companyName
    var longitude by Workplaces.longitude
    var latitude by Workplaces.latitude

    val users by User referrersOn Users.workplace
}

object LatestRefresh : IntIdTable() {
    val timestamp = timestamp("timestamp")
}
class Refresh(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Refresh>(LatestRefresh) {
        fun getLatestRefreshTime(): Instant {
            return findById(1)?.timestamp ?: new(1) { this.timestamp = Instant.EPOCH }.timestamp
        }

        fun updateLatestRefreshTime(updatedAt: Instant) {
            val refreshTime = findById(1) ?: new(1) { this.timestamp = Instant.EPOCH }
            refreshTime.timestamp = updatedAt
        }
    }

    var timestamp by LatestRefresh.timestamp
}
