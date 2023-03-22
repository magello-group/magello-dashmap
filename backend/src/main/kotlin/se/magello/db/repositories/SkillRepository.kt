package se.magello.db.repositories

import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.transactions.transaction
import se.magello.db.tables.Skill
import se.magello.db.tables.Skills
import se.magello.workflow.MagelloSkill
import se.magello.workflow.MagelloUserSkillWithUserInfo

class SkillRepository {
    fun getUserSkillsForSkillId(id: Int): List<MagelloUserSkillWithUserInfo> {
        return transaction {
            Skill.findById(id)?.let { skill ->
                skill.userSkills.map {
                    MagelloUserSkillWithUserInfo(
                        id = skill.id.value,
                        favourite = it.favourite,
                        masterSynonym = skill.masterSynonym,
                        synonyms = skill.synonyms?.split(";") ?: emptyList(),
                        level = it.level,
                        levelGoal = it.levelGoal,
                        levelGoalDeadline = it.levelGoalDeadline,
                        numberOfDaysWorkExperience = it.numberOfDaysWorkExperience,
                        userId = it.user.id.value,
                        firstName = it.user.firstName,
                        lastName = it.user.lastName
                    )
                }.sortedByDescending { it.level }
            } ?: emptyList()
        }
    }

    fun searchSkill(search: String): List<MagelloSkill> {
        return transaction {
            Skill.find(Skills.synonyms like "%$search%")
                .map { skill ->
                    MagelloSkill(
                        skill.id.value,
                        skill.masterSynonym,
                        skill.synonyms?.split(";") ?: emptyList()
                    )
                }
        }
    }
}