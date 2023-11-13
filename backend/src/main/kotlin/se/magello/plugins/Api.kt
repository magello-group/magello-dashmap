package se.magello.plugins

import io.ktor.resources.*
import kotlinx.serialization.Serializable

@Serializable
@Resource("/api")
class Api {
    @Serializable
    @Resource("/workplaces")
    data class Workplaces(val parent: Api = Api(), val limit: Int = 100, val offset: Long = 0) {
        @Serializable
        @Resource("/{organisationId}")
        data class Workplace(val parent: Workplaces = Workplaces(), val organisationId: String)
    }

    @Serializable
    @Resource("/users")
    data class Users(val parent: Api = Api(), val limit: Int = 100, val offset: Long = 0) {
        @Serializable
        @Resource("self")
        data class Self(val parent: Users = Users()) {
            @Serializable
            @Resource("preferences")
            data class Preferences(val parent: Self = Self())
        }

        @Serializable
        @Resource("/{userId}")
        data class Id(val parent: Users = Users(), val userId: Int)
    }

    @Serializable
    @Resource("/admin")
    data class Admin(val parent: Api = Api()) {
        @Serializable
        @Resource("/foodpreferences/export")
        data class ExportFoodPreferences(val parent: Admin = Admin())

        @Serializable
        @Resource("/coordinates")
        data class Coordinates(val parent: Admin = Admin()) {
            @Serializable
            @Resource("/unmapped")
            data class Unmapped(val parent: Coordinates = Coordinates(), val limit: Int = 100, val offset: Long = 0)

            @Serializable
            @Resource("/{id}")
            data class Organisation(val parent: Coordinates = Coordinates(), val id: String) {
                @Serializable
                @Resource("/edit")
                data class Edit(val parent: Organisation, val longitude: Double, val latitude: Double)
            }
        }
    }

    @Serializable
    @Resource("/skill")
    data class Skill(val parent: Api = Api()) {
        @Serializable
        @Resource("/search")
        data class Search(val parent: Skill = Skill(), val query: String)

        @Serializable
        @Resource("/{id}")
        data class Id(val parent: Skill = Skill(), val id: Int)
    }
}
