package se.magello.workflow

import java.time.Duration
import java.time.Instant
import kotlin.concurrent.timer
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.jetbrains.exposed.dao.load
import org.jetbrains.exposed.dao.with
import org.jetbrains.exposed.sql.transactions.transaction
import se.magello.db.Refresh
import se.magello.db.User
import se.magello.db.Workplace

private val logger = KotlinLogging.logger {}

/**
 * The workflow should run every day to update the User data we have in Salesforce and Cinode, this way we don't have to
 * fetch user data from Salesforce and Cinode upon request.
 */
class MergeUserDataWorkflow(private val worker: UserDataFetcher) {
    private var job: Job? = null

    init {
        timer(period = Duration.ofMinutes(10).toMillis()) {
            val updatedAt = transaction {
                Refresh.getLatestRefreshTime()
            }
            if (Duration.between(updatedAt, Instant.now()) > Duration.ofDays(1)) {
                runBlocking {
                    job = launch {
                        try {
                            worker.start()
                        } catch (e: Exception) {
                            logger.error(e) { "Failed to fetch and combine user data" }
                        }
                    }
                }
            }
        }
    }

    // TODO: Move to a database client instead
    fun getAllUsers(limit: Int, offset: Int): List<MagelloUser> {
        if (job?.isActive == true) {
            throw JobRunningException("Job is currently running")
        }
        return transaction {
            User.all()
                .with(User::workplace, User::skills)
                .drop(offset)
                .take(limit)
                .mapToMagelloUser()
        }
    }

    // TODO: Move to a database client instead
    fun getWorkAssignment(organisationId: String): MagelloWorkAssignment? {
        if (job?.isActive == true) {
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
                            user.title
                        )
                    }
                )
            } else {
                null
            }
        }
    }

    // TODO: Move to a database client instead
    fun getAllWorkAssignments(limit: Int, offset: Int): List<MagelloWorkAssignment> {
        if (job?.isActive == true) {
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
                                user.title
                            )
                        }
                    )
                }
        }
    }
}

fun List<User>.mapToMagelloUser() = this.map { user ->
    MagelloUser(
        user.id.value,
        user.email,
        user.firstName,
        user.imageUrl,
        user.lastName,
        user.title,
        user.skills.map { skill ->
            MagelloSkill(
                skill.id.value,
                skill.favourite,
                skill.masterSynonym,
                skill.synonyms?.split(";") ?: emptyList(),
                skill.level,
                skill.levelGoal,
                skill.levelGoalDeadline,
                skill.numberOfDaysWorkExperience
            )
        },
        MagelloWorkAssignment(
            user.workplace.id.value,
            user.workplace.companyName,
            user.workplace.longitude,
            user.workplace.latitude
        )
    )
}
