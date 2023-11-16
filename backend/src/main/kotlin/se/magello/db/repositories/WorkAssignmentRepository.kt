package se.magello.db.repositories

import org.jetbrains.exposed.dao.load
import org.jetbrains.exposed.dao.with
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.transactions.transaction
import se.magello.db.tables.*
import se.magello.workflow.*

class WorkAssignmentRepository(private val workflow: MergeUserDataWorkflow) {
    fun getWorkAssignment(organisationId: String): MagelloWorkAssignment? {
        if (workflow.isJobRunning()) {
            throw JobRunningException("Job is currently running")
        }
        return transaction {
            Workplace.findById(organisationId)
                ?.load(Workplace::users)
                ?.let { workplace ->
                    val coordinates = MappedCoordinates.findById(organisationId)
                    MagelloWorkAssignment(
                        workplace.id.value,
                        workplace.companyName,
                        fromPoints(coordinates?.longitude, coordinates?.latitude),
                        workplace.users.map { user ->
                            StrippedMagelloUser(
                                user.id.value,
                                user.email,
                                user.firstName,
                                user.imageUrl,
                                user.lastName,
                                user.title,
                                user.preferences?.quote,
                                user.userSkills
                                    .orderBy(UserSkills.favourite to SortOrder.DESC)
                                    .limit(3)
                                    .filter { it.favourite }
                                    .map { skill ->
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

    fun getAllWorkAssignments(limit: Int, offset: Long): List<MagelloWorkPlace> {
        if (workflow.isJobRunning()) {
            throw JobRunningException("Job is currently running")
        }
        return transaction {
            Workplace.all()
                .limit(limit, offset)
                .map { workplace ->
                    val coordinates = MappedCoordinates.findById(workplace.id.value)
                    MagelloWorkPlace(
                        organisationId = workplace.id.value,
                        companyName = workplace.companyName,
                        coordinates = fromPoints(coordinates?.longitude, coordinates?.latitude),
                    )
                }
        }
    }
}