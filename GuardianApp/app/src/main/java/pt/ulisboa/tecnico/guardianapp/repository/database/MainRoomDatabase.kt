package pt.ulisboa.tecnico.guardianapp.repository.database

import androidx.room.Database
import androidx.room.RoomDatabase
import pt.ulisboa.tecnico.guardianapp.repository.database.dao.MainDAO
import pt.ulisboa.tecnico.guardianapp.repository.database.entity.LocationDBEntity

@Database(entities = [LocationDBEntity::class], version = 2)
abstract class MainRoomDatabase : RoomDatabase() {
    abstract fun mainDAO(): MainDAO
}