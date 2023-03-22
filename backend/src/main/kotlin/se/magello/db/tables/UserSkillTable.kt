package se.magello.db.tables

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable

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
