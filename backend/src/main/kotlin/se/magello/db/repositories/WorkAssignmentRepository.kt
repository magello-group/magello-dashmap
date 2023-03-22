package se.magello.db.repositories

import org.jetbrains.exposed.dao.load
import org.jetbrains.exposed.dao.with
import org.jetbrains.exposed.sql.transactions.transaction
import se.magello.db.tables.Workplace
import se.magello.workflow.*

class WorkAssignmentRepository(private val workflow: MergeUserDataWorkflow) {
    fun getWorkAssignment(organisationId: String): MagelloWorkAssignment? {
        if (workflow.isJobRunning()) {
            throw JobRunningException("Job is currently running")
        }
        return transaction {
            val workplace = Workplace.findById(organisationId)?.load(Workplace::users)
            if (workplace != null) {
                MagelloWorkAssignment(
                    workplace.id.value,
                    workplace.companyName,
                    workplace.longitude,
                    workplace.latitude,
                    workplace.users.map { user ->
                        StrippedMagelloUser(
                            user.id.value,
                            user.email,
                            user.firstName,
                            user.imageUrl,
                            user.lastName,
                            user.title,
                            user.preferences?.quote,
                            user.userSkills.map { skill ->
                                    MagelloUserSkill(
                                        id = skill.skill.id.value,
                                        favourite = skill.favourite,
                                        masterSynonym = skill.skill.masterSynonym,
                                        synonyms = skill.skill.synonyms?.split(";") ?: emptyList(),
                                        level = skill.level,
                                        levelGoal = skill.levelGoal,
                                        levelGoalDeadline = skill.levelGoalDeadline,
                                        numberOfDaysWorkExperience = skill.numberOfDaysWorkExperience
                                    )
                                }
                        )
                    }
                )
            } else {
                null
            }
        }
    }

    fun getAllWorkAssignments(limit: Int, offset: Int): List<MagelloWorkAssignment> {
        if (workflow.isJobRunning()) {
            throw JobRunningException("Job is currently running")
        }
        return transaction {
            Workplace.all()
                .with(Workplace::users)
                .drop(offset)
                .take(limit)
                .map {
                    MagelloWorkAssignment(
                        organisationId = it.id.value,
                        companyName = it.companyName,
                        longitude = it.longitude,
                        latitude = it.latitude,
                        users = it.users.map { user ->
                            StrippedMagelloUser(
                                id = user.id.value,
                                email = user.email,
                                firstName = user.firstName,
                                imageUrl = user.imageUrl,
                                lastName = user.lastName,
                                title = user.title,
                                quote = user.preferences?.quote,
                                userSkills = user.userSkills.map { skill ->
                                    MagelloUserSkill(
                                        id = skill.skill.id.value,
                                        favourite = skill.favourite,
                                        masterSynonym = skill.skill.masterSynonym,
                                        synonyms = skill.skill.synonyms?.split(";") ?: emptyList(),
                                        level = skill.level,
                                        levelGoal = skill.levelGoal,
                                        levelGoalDeadline = skill.levelGoalDeadline,
                                        numberOfDaysWorkExperience = skill.numberOfDaysWorkExperience
                                    )
                                }
                            )
                        }
                    )
                }
        }
    }
}