package pt.ulisboa.tecnico.guardianapp.repository.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "Location", primaryKeys = ["childId", "timestamp"])
class LocationDBEntity(
    @ColumnInfo(name = "childId")
    val childId: String,

    @ColumnInfo(name = "latitude")
    val latitude: Double,

    @ColumnInfo(name = "longitude")
    val longitude: Double,

    @ColumnInfo(name = "timestamp")
    val timestamp: String
)