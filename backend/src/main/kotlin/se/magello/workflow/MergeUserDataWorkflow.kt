package se.magello.workflow

import java.time.Duration
import java.time.Instant
import kotlin.concurrent.timer
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.transaction
import se.magello.db.Refresh
import se.magello.db.User

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
            if (Duration.between(updatedAt, Instant.now()) > Duration.ofDays(100)) {
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

    fun isJobRunning(): Boolean {
        return job?.isActive ?: false
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
        user.userSkills.map { userSkill ->
            MagelloUserSkill(
                userSkill.skill.id.value,
                userSkill.favourite,
                userSkill.skill.masterSynonym,
                userSkill.skill.synonyms?.split(";") ?: emptyList(),
                userSkill.level,
                userSkill.levelGoal,
                userSkill.levelGoalDeadline,
                userSkill.numberOfDaysWorkExperience
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
