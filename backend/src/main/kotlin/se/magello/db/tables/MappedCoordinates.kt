package se.magello.db.tables

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.dao.id.EntityID

/**
 * Coordinates for Workplaces table with DAO
 */
object MappedCoordinatesTable : IdTable<String>(name = "workplace_coordinates") {
    override val id = varchar("id", 25).entityId()
    val companyName = varchar("companyName", 1024).index()
    val longitude = double("longitude").nullable()
    val latitude = double("latitude").nullable()
}
class MappedCoordinates(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, MappedCoordinates>(MappedCoordinatesTable)
    var companyName by MappedCoordinatesTable.companyName
    var longitude by MappedCoordinatesTable.longitude
    var latitude by MappedCoordinatesTable.latitude
}
