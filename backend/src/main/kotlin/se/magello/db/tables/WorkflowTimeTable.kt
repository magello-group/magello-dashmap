package se.magello.db.tables

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant


object LatestRefresh : IntIdTable() {
    val timestamp = timestamp("timestamp")
}
class Refresh(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Refresh>(LatestRefresh) {
        fun getLatestRefreshTime(): Instant {
            return findById(1)?.timestamp ?: new(1) { this.timestamp = Instant.EPOCH }.timestamp
        }

        fun updateLatestRefreshTime(updatedAt: Instant) {
            val refreshTime = findById(1) ?: new(1) { this.timestamp = Instant.EPOCH }
            refreshTime.timestamp = updatedAt
        }
    }

    var timestamp by LatestRefresh.timestamp
}
