package se.magello

import com.typesafe.config.ConfigFactory
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import java.time.Instant
import kotlin.test.assertEquals
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Test
import se.magello.db.LatestRefresh
import se.magello.db.Refresh
import se.magello.db.Skill
import se.magello.db.Skills
import se.magello.db.User
import se.magello.db.UserSkills
import se.magello.db.Users
import se.magello.db.Workplace
import se.magello.db.Workplaces
import se.magello.map.EniroAddressLookupClient.Companion.MAGELLO_OFFICE_COORDINATES
import se.magello.plugins.configureRouting
import se.magello.salesforce.SalesForceVersion
import se.magello.salesforce.responses.QueryResponse
import se.magello.workflow.UserDataFetcher.Companion.MAGELLO_OFFICE

class ApplicationTest {
    @Test
    fun testRoot() = testApplication {
        val applicationConfig = ConfigFactory.defaultApplication()
        application {
            configureRouting(applicationConfig.getConfig("routing"))
        }
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Hello World!", bodyAsText())
        }
    }

    @Test
    fun testDatabaseInsert() {
        Database.connect("jdbc:sqlite:dashmap.db", user = "root", password = "test")

        transaction {
            SchemaUtils.drop(Users, Skills, Workplaces, UserSkills, inBatch = true)

            SchemaUtils.create(Users, Skills, Workplaces, UserSkills, LatestRefresh, inBatch = true)

            println(Refresh.getLatestRefreshTime())

            Refresh.updateLatestRefreshTime(Instant.now())

            println(Refresh.getLatestRefreshTime())
        }

        val userSkills = transaction {
            listOf(
                Skill.new(1) {
                    masterSynonym = "Java"
                    favourite = false
                    synonyms = arrayOf("Java EE", "Java 8", "lolcode").joinToString(";")
                    level = 3
                },
                Skill.new(2) {
                    masterSynonym = "Go"
                    favourite = true
                    synonyms = arrayOf("Golang").joinToString(";")
                    level = 2
                }
            )
        }

        transaction {
            val newWorkplace = Workplace.new(55551102) {
                companyName = MAGELLO_OFFICE.name
                longitude = MAGELLO_OFFICE_COORDINATES.lon
                latitude = MAGELLO_OFFICE_COORDINATES.lat
            }
            User.new(123) {
                email = "fabian.eriksson@magello.se"
                firstName = "Fabian"
                lastName = "Eriksson"
                imageUrl = "https://example.com"
                title = "Coder"
                workplace = newWorkplace
                skills = SizedCollection(userSkills)
            }
        }

        transaction {
            val fabbe = User.findById(123) ?: throw Error("Failed to fetch fabian")
            println(fabbe.email)

            fabbe.skills.forEach {
                println("${it.masterSynonym} - ${it.level}. Is a favourite? ${it.favourite}")
                println(it.synonyms)
            }

            val magello = Workplace.find { Workplaces.companyName eq MAGELLO_OFFICE.name }.single()

            magello.users.forEach {
                println("Här jobbar: ${it.firstName}")
            }
        }
    }

    @Test
    fun testDecodeVersion() {
        val json = Json {
            ignoreUnknownKeys = true
        }
        val string =
            "[{\"label\":\"Summer '14\",\"url\":\"/services/data/v31.0\",\"version\":\"31.0\"},{\"label\":\"Winter '15\",\"url\":\"/services/data/v32.0\",\"version\":\"32.0\"},{\"label\":\"Spring '15\",\"url\":\"/services/data/v33.0\",\"version\":\"33.0\"},{\"label\":\"Summer '15\",\"url\":\"/services/data/v34.0\",\"version\":\"34.0\"},{\"label\":\"Winter '16\",\"url\":\"/services/data/v35.0\",\"version\":\"35.0\"},{\"label\":\"Spring '16\",\"url\":\"/services/data/v36.0\",\"version\":\"36.0\"},{\"label\":\"Summer '16\",\"url\":\"/services/data/v37.0\",\"version\":\"37.0\"},{\"label\":\"Winter '17\",\"url\":\"/services/data/v38.0\",\"version\":\"38.0\"},{\"label\":\"Spring '17\",\"url\":\"/services/data/v39.0\",\"version\":\"39.0\"},{\"label\":\"Summer '17\",\"url\":\"/services/data/v40.0\",\"version\":\"40.0\"},{\"label\":\"Winter '18\",\"url\":\"/services/data/v41.0\",\"version\":\"41.0\"},{\"label\":\"Spring ’18\",\"url\":\"/services/data/v42.0\",\"version\":\"42.0\"},{\"label\":\"Summer '18\",\"url\":\"/services/data/v43.0\",\"version\":\"43.0\"},{\"label\":\"Winter '19\",\"url\":\"/services/data/v44.0\",\"version\":\"44.0\"},{\"label\":\"Spring '19\",\"url\":\"/services/data/v45.0\",\"version\":\"45.0\"},{\"label\":\"Summer '19\",\"url\":\"/services/data/v46.0\",\"version\":\"46.0\"},{\"label\":\"Winter '20\",\"url\":\"/services/data/v47.0\",\"version\":\"47.0\"},{\"label\":\"Spring '20\",\"url\":\"/services/data/v48.0\",\"version\":\"48.0\"},{\"label\":\"Summer '20\",\"url\":\"/services/data/v49.0\",\"version\":\"49.0\"},{\"label\":\"Winter '21\",\"url\":\"/services/data/v50.0\",\"version\":\"50.0\"},{\"label\":\"Spring '21\",\"url\":\"/services/data/v51.0\",\"version\":\"51.0\"},{\"label\":\"Summer '21\",\"url\":\"/services/data/v52.0\",\"version\":\"52.0\"},{\"label\":\"Winter '22\",\"url\":\"/services/data/v53.0\",\"version\":\"53.0\"},{\"label\":\"Spring '22\",\"url\":\"/services/data/v54.0\",\"version\":\"54.0\"},{\"label\":\"Summer '22\",\"url\":\"/services/data/v55.0\",\"version\":\"55.0\"},{\"label\":\"Winter '23\",\"url\":\"/services/data/v56.0\",\"version\":\"56.0\"},{\"label\":\"Spring '23\",\"url\":\"/services/data/v57.0\",\"version\":\"57.0\"}]\n"

        val temp: List<SalesForceVersion> = json.decodeFromString(string)
        println(temp.toString())
    }

    @Test
    fun testDecodeRecord() {
        val incomingResponse = """
            {
              "totalSize": 1,
              "done": true,
              "records": [
                {
                  "attributes": {
                    "type": "Avtal__c",
                    "url": "/services/data/v57.0/sobjects/Avtal__c/a085r000002vYdhAAE"
                  },
                  "Name": "Fabian Eriksson",
                  "Avtalspart__r": {
                    "attributes": {
                      "type": "Account",
                      "url": "/services/data/v57.0/sobjects/Account/0015r00000fH8LTAA0"
                    },
                    "Name": "Test AB",
                    "Organisationsnummer__c": "556531-7129"
                  }
                }
              ]
            }
        """.trimIndent()

        val json = Json {
            ignoreUnknownKeys = true
        }

        val response = json.decodeFromString<QueryResponse>(incomingResponse)

        println(response)

        val string = json.encodeToString(response)

        println(string)
    }
}