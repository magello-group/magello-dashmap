package se.magello.db

import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.vendors.currentDialect

// TODO: Look into how to create an array type so we can skip splitting and joining arrays/strings in calls
class ArrayColumnType: ColumnType() {
    override fun sqlType(): String = ""
}