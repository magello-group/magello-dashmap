package se.magello.workflow

import io.ktor.server.auth.jwt.JWTPrincipal
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
import se.magello.db.UserPreference
import se.magello.db.Users
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

    fun postUserPreferences(principal: JWTPrincipal, preferences: MagelloUserPreferences) {
        val email = principal.getClaim("email", String::class)
            ?: throw IllegalStateException("This user is not allowed to update preferences")

        transaction {
            val user = User.find { Users.email eq email }.firstOrNull()
                ?: throw IllegalStateException("No user with email $email exists")

            val userPreferences = UserPreference.findById(email)
            if (userPreferences == null) {
                val newPreferences = UserPreference.new(email) {
                    dietPreferences = preferences.dietPreferences.joinToString(";")
                    extraDietPreferences = preferences.extraDietPreferences
                    socials = preferences.socials.joinToString(";") { socialUrl -> socialUrl.url }
                    quote = preferences.quote
                }
                user.preferences = newPreferences
            } else {
                userPreferences.dietPreferences = preferences.dietPreferences.joinToString(";")
                userPreferences.extraDietPreferences = preferences.extraDietPreferences
                userPreferences.socials = preferences.socials.joinToString(";") { socialUrl -> socialUrl.url }
                userPreferences.quote = preferences.quote
            }
        }
    }

    fun getUserSelf(principal: JWTPrincipal): MagelloUserSelf? {
        val email = principal.getClaim("email", String::class) ?: return null

        return transaction {
            val user = User.find { Users.email eq email }
                .with(User::workplace, User::skills, User::preferences)
                .singleOrNull()
            if (user != null) {
                MagelloUserSelf(
                    id = user.id.value,
                    email = user.email,
                    firstName = user.firstName,
                    imageUrl = user.imageUrl,
                    lastName = user.lastName,
                    title = user.title,
                    skills = user.skills.map { skill ->
                        MagelloSkill(
                            id = skill.id.value,
                            favourite = skill.favourite,
                            masterSynonym = skill.masterSynonym,
                            synonyms = skill.synonyms?.split(";") ?: emptyList(),
                            level = skill.level,
                            levelGoal = skill.levelGoal,
                            levelGoalDeadline = skill.levelGoalDeadline,
                            numberOfDaysWorkExperience = skill.numberOfDaysWorkExperience
                        )
                    },
                    assignment = MagelloWorkAssignment(
                        organisationId = user.workplace.id.value,
                        companyName = user.workplace.companyName,
                        longitude = user.workplace.longitude,
                        latitude = user.workplace.latitude,
                    ),
                    preferences = user.preferences?.let {
                        MagelloUserPreferences(
                            it.dietPreferences.split(";"),
                            it.extraDietPreferences,
                            it.socials.split(";").map { url -> SocialUrl(url) },
                            it.quote
                        )
                    }
                )
            } else {
                null
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
                                user.title,
                                user.preferences?.quote
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