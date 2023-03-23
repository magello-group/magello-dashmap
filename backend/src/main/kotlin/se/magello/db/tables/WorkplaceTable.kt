package se.magello.db.tables

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable

/**
 * Workplaces table with DAO
 */
object Workplaces : IdTable<String>() {
    override val id = varchar("id", 25).entityId()
    val companyName = varchar("companyName", 1024).index()
    val longitude = double("longitude").nullable()
    val latitude = double("latitude").nullable()
}
class Workplace(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, Workplace>(Workplaces)
    var companyName by Workplaces.companyName
    var longitude by Workplaces.longitude
    var latitude by Workplaces.latitude

    val users by User referrersOn Users.workplace
}
