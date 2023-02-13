package se.magello.workflow

import java.time.Instant
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.transactions.transaction
import se.magello.cinode.CinodeClient
import se.magello.cinode.CinodeSkill
import se.magello.cinode.CinodeUser
import se.magello.db.Refresh
import se.magello.db.Skill
import se.magello.db.Skills
import se.magello.db.User
import se.magello.db.UserSkills
import se.magello.db.Users
import se.magello.db.Workplace
import se.magello.db.Workplaces
import se.magello.map.EniroAddressLookupClient
import se.magello.map.EniroAddressLookupClient.Companion.MAGELLO_OFFICE_COORDINATES
import se.magello.salesforce.SalesForceClient
import se.magello.salesforce.responses.Attributes
import se.magello.salesforce.responses.RecordType

/*
 * Ideas:
 *  - Where to lunch, find the best suitable lunch place between two assignments.
 */

/**
 * Combines user data from Salesforce, Cinode and company address information from Eniro in order to map and save the
 * data in the format as we want it.
 */
class UserDataFetcher(
    private val cinodeClient: CinodeClient,
    private val salesForceClient: SalesForceClient,
    private val addressLookupClient: EniroAddressLookupClient
) {
    suspend fun start() {
        val cinodeUsers = cinodeClient.getAllUsers()
        val salesForceUsers = salesForceClient.getAllMagelloUsers()
            ?.records?.filterIsInstance<RecordType.Agreement>() ?: emptyList()

        val userAgreements = findAgreementForUser(cinodeUsers, salesForceUsers)

        val usersToPersist = userAgreements.map { combineUserWithExtras(it.key, it.value) }

        // TODO: Handle exceptions and make it available for admin pages.
        resetDataTables()
        usersToPersist.forEach { saveUserToDatabase(it) }
        transaction {
            Refresh.updateLatestRefreshTime(Instant.now())
        }
    }

    private fun findAgreementForUser(
        cinodeUsers: List<CinodeUser>,
        salesForceAgreements: List<RecordType.Agreement>
    ): Map<CinodeUser, RecordType.Agreement> {
        return cinodeUsers.associateWith { cinodeUser ->
            val now = Instant.now()

            // Currently the name is what we use to map Cinode users with Salesforce users.
            val nameId = "${cinodeUser.firstName} ${cinodeUser.lastName}"
            val agreementForUser = salesForceAgreements.find {
                it.fullName == nameId && now.isBefore(it.endDate)
            } ?: RecordType.Agreement(
                nameId,
                Attributes("n/a"),
                Instant.MAX,
                MAGELLO_OFFICE
            )

            agreementForUser
        }
    }

    private suspend fun combineUserWithExtras(cinodeUser: CinodeUser, agreement: RecordType.Agreement): MagelloUser {
        val skills = cinodeClient.getSkillsForUser(cinodeUser.companyUserId)
            .toMagelloSkills()

        val workAssignment = if (agreement.relatedAccount !is RecordType.Account) {
            MagelloWorkAssignment(
                organisationId = MAGELLO_OFFICE.organisationId,
                companyName = MAGELLO_OFFICE.name,
                longitude = MAGELLO_OFFICE_COORDINATES.lon,
                latitude = MAGELLO_OFFICE_COORDINATES.lat
            )
        } else {
            val organisationPosition = addressLookupClient.lookupOrganisationPosition(
                agreement.relatedAccount.organisationId
            )

            MagelloWorkAssignment(
                agreement.relatedAccount.organisationId,
                agreement.relatedAccount.name,
                organisationPosition.lon,
                organisationPosition.lat
            )
        }


        return MagelloUser(
            id = cinodeUser.companyUserId,
            email = cinodeUser.companyUserEmail ?: "unknown@magello.se",
            firstName = cinodeUser.firstName,
            imageUrl = cinodeUser.image?.largeImageUrl ?: cinodeUser.image?.url,
            lastName = cinodeUser.lastName,
            title = cinodeUser.title,
            skills = skills,
            assignment = workAssignment
        )
    }

    private fun resetDataTables() {
        transaction {
            SchemaUtils.drop(Skills, Workplaces, Users, UserSkills, inBatch = true)
            SchemaUtils.create(Skills, Workplaces, Users, UserSkills, inBatch = true)
        }
    }

    private fun saveUserToDatabase(user: MagelloUser) {
        val userSkills = transaction {
            user.skills.map {
                Skill.findById(it.id) ?: Skill.new(it.id) {
                    favourite = it.favourite ?: false
                    masterSynonym = it.masterSynonym
                    synonyms = it.synonyms.joinToString(";")
                    level = it.level
                    levelGoal = it.levelGoal
                    levelGoalDeadline = it.levelGoalDeadline
                    numberOfDaysWorkExperience = it.numberOfDaysWorkExperience
                }
            }
        }

        transaction {
            val workplace = Workplace.findById(
                user.assignment.organisationId
            ) ?: Workplace.new(user.assignment.organisationId) {
                companyName = user.assignment.companyName
                longitude = user.assignment.longitude
                latitude = user.assignment.latitude
            }

            User.new(user.id) {
                firstName = user.firstName
                lastName = user.lastName
                email = user.email
                imageUrl = user.imageUrl
                title = user.title
                this.workplace = workplace
                skills = SizedCollection(userSkills)
            }
        }
    }

    companion object {
        val MAGELLO_OFFICE = RecordType.Account("Magello -- Mellan uppdrag", Attributes("n/a"), "556531-7129")
    }
}

@Serializable
data class MagelloUser(
    val id: Int,
    val email: String,
    val firstName: String,
    val imageUrl: String?,
    val lastName: String,
    val title: String?,
    val skills: List<MagelloSkill> = emptyList(),
    val assignment: MagelloWorkAssignment
)

@Serializable
data class MagelloUserSelf(
    val id: Int,
    val email: String,
    val firstName: String,
    val imageUrl: String?,
    val lastName: String,
    val title: String?,
    val skills: List<MagelloSkill> = emptyList(),
    val assignment: MagelloWorkAssignment,
    val preferences: MagelloUserPreferences?
)

@Serializable
data class MagelloUserPreferences(
    val dietPreferences: List<String> = emptyList(),
    val extraDietPreferences: String?,
    val socials: List<SocialUrl> = emptyList(),
    val quote: String?
)

@Serializable
data class SocialUrl(val url: String)

@Serializable
data class StrippedMagelloUser(
    val id: Int,
    val email: String,
    val firstName: String,
    val imageUrl: String?,
    val lastName: String,
    val title: String?,
    val quote: String?
)

@Serializable
data class MagelloSkill(
    val id: Int,
    val favourite: Boolean?,
    val masterSynonym: String,
    val synonyms: List<String>,
    val level: Int?,
    val levelGoal: Int?,
    val levelGoalDeadline: String?,
    val numberOfDaysWorkExperience: Int?,
)

fun List<CinodeSkill>.toMagelloSkills() = this.map {
    MagelloSkill(
        it.id,
        it.favourite,
        it.keyword?.masterSynonym ?: "Unknown skill",
        it.keyword?.synonyms ?: emptyList(),
        it.level,
        it.levelGoal,
        it.levelGoalDeadline,
        it.numberOfDaysWorkExperience
    )
}

@Serializable
data class MagelloWorkAssignment(
    val organisationId: String,
    val companyName: String,
    val longitude: Double,
    val latitude: Double,
    val users: List<StrippedMagelloUser> = emptyList()
)