package se.magello.db.tables

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

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
