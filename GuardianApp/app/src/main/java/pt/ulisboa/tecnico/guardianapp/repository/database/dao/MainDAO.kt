package pt.ulisboa.tecnico.guardianapp.repository.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import pt.ulisboa.tecnico.guardianapp.model.Location
import pt.ulisboa.tecnico.guardianapp.repository.database.entity.LocationDBEntity

@Dao
interface MainDAO {

    @Query("select * from Location where childId = :childId and timestamp = (select max(timestamp) from Location where childId = :childId)")
    fun getLastLocation(childId: String): LiveData<LocationDBEntity>

    @Query("select * from Location where childId = :childId")
    fun getLocationHistory(childId: String): LiveData<List<Location>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLocation(locationDBEntity: LocationDBEntity)
}