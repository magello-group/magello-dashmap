package se.magello.db.repositories

import org.jetbrains.exposed.dao.load
import org.jetbrains.exposed.dao.with
import org.jetbrains.exposed.sql.transactions.transaction
import se.magello.db.Workplace
import se.magello.workflow.JobRunningException
import se.magello.workflow.MagelloWorkAssignment
import se.magello.workflow.MergeUserDataWorkflow
import se.magello.workflow.StrippedMagelloUser

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
                            user.preferences?.quote
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
                        it.id.value,
                        it.companyName,
                        it.longitude,
                        it.latitude,
                        it.users.map { user ->
                            StrippedMagelloUser(
                                user.id.value,
                                user.email,
                                user.firstName,
                                user.imageUrl,
                                user.lastName,
                                user.title,
                                user.preferences?.quote
                            )
                        }
                    )
                }
        }
    }
}