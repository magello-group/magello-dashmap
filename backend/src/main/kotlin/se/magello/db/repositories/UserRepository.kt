package se.magello.db.repositories

import io.ktor.server.auth.jwt.*
import org.jetbrains.exposed.dao.load
import org.jetbrains.exposed.dao.with
import org.jetbrains.exposed.sql.transactions.transaction
import se.magello.db.tables.User
import se.magello.db.tables.UserPreference
import se.magello.db.tables.Users
import se.magello.plugins.isAdmin
import se.magello.workflow.*

class UserRepository(private val workflow: MergeUserDataWorkflow) {
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
                user.preferences = userPreferences
            }
        }
    }

    fun getUserSelf(principal: JWTPrincipal): MagelloUserSelf? {
        val email = principal.getClaim("email", String::class) ?: return null

        return transaction {
            val user = User.find { Users.email eq email }
                .with(User::workplace, User::userSkills, User::preferences)
                .singleOrNull()
            if (user != null) {
                MagelloUserSelf(
                    id = user.id.value,
                    email = user.email,
                    firstName = user.firstName,
                    imageUrl = user.imageUrl,
                    lastName = user.lastName,
                    isAdmin = principal.isAdmin(),
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
                        MagelloUserPreferences(
                            dietPreferences = it.dietPreferences.split(";"),
                            extraDietPreferences = it.extraDietPreferences,
                            socials = it.socials.split(";").map { url -> SocialUrl(url) },
                            quote = it.quote
                        )
                    }
                )
            } else {
                null
            }
        }
    }

    fun getUser(userId: Int): PublicMagelloUser? {
        if (workflow.isJobRunning()) {
            throw JobRunningException("Job is currently running")
        }
        return transaction {
            User.findById(userId)
                ?.load(User::workplace, User::userSkills, User::preferences)
                ?.let { user ->
                    mapToMagelloUser(user)
                }
        }
    }

    fun getAllUserPreferences(): List<ExportMagelloUser> {
        if (workflow.isJobRunning()) {
            throw JobRunningException("Job is currently running")
        }

        return transaction {
            User.all()
                .with(User::preferences)
                .map { user ->
                    ExportMagelloUser(
                        email = user.email,
                        firstName = user.firstName,
                        lastName = user.lastName,
                        dietPreferences = user.preferences?.dietPreferences?.split(";") ?: emptyList(),
                        extraDietPreferences = user.preferences?.extraDietPreferences
                    )
                }
        }
    }

    fun getAllUsers(limit: Int, offset: Int): List<PublicMagelloUser> {
        if (workflow.isJobRunning()) {
            throw JobRunningException("Job is currently running")
        }
        return transaction {
            User.all()
                .with(User::workplace, User::userSkills)
                .drop(offset)
                .take(limit)
                .mapToMagelloUser()
        }
    }
}