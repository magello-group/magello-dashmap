package se.magello.db.tables

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable

/**
 * Workplaces table with DAO
 */
object Workplaces : IdTable<String>() {
    override val id = varchar("id", 25).entityId().uniqueIndex()
    val companyName = varchar("companyName", 1024).index()
}
class Workplace(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, Workplace>(Workplaces)
    var companyName by Workplaces.companyName

    val users by User referrersOn Users.workplace
}
