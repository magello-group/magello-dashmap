package se.magello.workflow

import java.time.Duration
import java.time.Instant
import kotlin.concurrent.timer
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.transaction
import se.magello.db.tables.Refresh
import se.magello.db.tables.User

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
                            logger.info { "Starting Salesforce/Cinode integration job" }

                            worker.start()

                            logger.info { "Completed Salesforce/Cinode integration job" }
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
    mapToMagelloUser(user)
}

fun mapToMagelloUser(user: User): PublicMagelloUser {
    return PublicMagelloUser(
        id = user.id.value,
        email = user.email,
        firstName = user.firstName,
        imageUrl = user.imageUrl,
        lastName = user.lastName,
        title = user.title,
        skills = user.userSkills.map { userSkill ->
            MagelloUserSkill(
                id = userSkill.skill.id.value,
                favourite = userSkill.favourite,
                masterSynonym = userSkill.skill.masterSynonym,
                synonyms = userSkill.skill.synonyms?.split(";") ?: emptyList(),
                level = userSkill.level,
                levelGoal = userSkill.levelGoal,
                levelGoalDeadline = userSkill.levelGoalDeadline,
                numberOfDaysWorkExperience = userSkill.numberOfDaysWorkExperience
            )
        },
        assignment = MagelloWorkAssignment(
            organisationId = user.workplace.id.value,
            companyName = user.workplace.companyName,
            coordinates = fromPoints(user.workplace.longitude, user.workplace.latitude)
        ),
        preferences = user.preferences?.let {
            MagelloUserPublicPreferences(
                socials = it.socials.split(";").map { url -> SocialUrl(url) },
                quote = it.quote
            )
        }
    )
}
