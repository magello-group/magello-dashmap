package se.magello.db

import java.time.Instant
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

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
}
class User(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<User>(Users)
    var email by Users.email
    var firstName by Users.firstName
    var lastName by Users.lastName
    var imageUrl by Users.imageUrl
    var title by Users.title

    var skills by Skill via UserSkills
    var workplace by Workplace referencedOn Users.workplace
}

/**
 * User skills Table with DAO
 */
object Skills : IntIdTable() {
    val favourite = bool("favourite")
    val masterSynonym = varchar("masterSynonym", 1024).index()
    val synonyms = text("synonyms").nullable()
    val level = integer("level").nullable()
    val levelGoal = integer("levelGoal").nullable()
    val levelGoalDeadline = varchar("levelGoalDeadline", 1024).nullable()
    val numberOfDaysWorkExperience = integer("numberOfDaysWorkExperience").nullable()
}
class Skill(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Skill>(Skills)
    var favourite by Skills.favourite
    var masterSynonym by Skills.masterSynonym
    var synonyms by Skills.synonyms
    var level by Skills.level
    var levelGoal by Skills.levelGoal
    var levelGoalDeadline by Skills.levelGoalDeadline
    var numberOfDaysWorkExperience by Skills.numberOfDaysWorkExperience

    val users by User via UserSkills
}

/**
 * User-to-skills mapping
 */
object UserSkills : Table() {
    val user = reference("user", Users)
    val skill = reference("skill", Skills)
    override val primaryKey = PrimaryKey(user, skill)
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
