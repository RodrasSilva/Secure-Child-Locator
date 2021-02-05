package pt.ulisboa.tecnico.guardianapp.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import pt.ulisboa.tecnico.guardianapp.App
import pt.ulisboa.tecnico.guardianapp.model.Location
import pt.ulisboa.tecnico.guardianapp.model.toLocation
import pt.ulisboa.tecnico.guardianapp.repository.database.dao.MainDAO
import pt.ulisboa.tecnico.guardianapp.repository.database.entity.LocationDBEntity
import pt.ulisboa.tecnico.guardianapp.repository.service.ServerApi
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

object LocationRepository {

    private lateinit var context: App
    private lateinit var dao: MainDAO
    private lateinit var executor: ScheduledThreadPoolExecutor

    private var lastLocation: Location? = null


    fun init(context: Context, dao: MainDAO) {
        this.context = context as App
        this.dao = dao
    }

    fun getLastLocation(): LiveData<Location?> {
        val dbLd = dao.getLastLocation(context.childId)
        return Transformations.map(dbLd) {
            if (it != null) Location(it.latitude, it.longitude, it.timestamp) else null
        }
    }

    fun getLocationHistory(): LiveData<List<Location>> {
        val dbLd = dao.getLocationHistory(context.childId)
        return Transformations.map(dbLd) {
            it?.map { locationDb ->
                Location(locationDb.latitude, locationDb.longitude, locationDb.timestamp)
            }
                ?: emptyList()
        }
    }

    fun enableLocationUpdates(interval: Long) {
        executor = ScheduledThreadPoolExecutor(1)
        executor.scheduleAtFixedRate({
            val locationString = ServerApi.getLocation(context.uniqueId, context.childId)
            val location = if (locationString.isEmpty()) null else locationString.toLocation()
            if (location != null) {
                if (lastLocation == null) {
                    lastLocation = location
                    dao.insertLocation(LocationDBEntity(context.childId, location.latitude, location.longitude, location.timestamp))
                } else {
                    if (isNewLocation(location)) {
                        lastLocation = location
                        dao.insertLocation(LocationDBEntity(context.childId, location.latitude, location.longitude, location.timestamp))
                    }
                }
            }
        }, 0, interval, TimeUnit.SECONDS)
    }

    private fun isNewLocation(location: Location): Boolean {
        val lastLocationTimeStamp = LocalDateTime.parse(lastLocation?.timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val newLocationTimeStamp = LocalDateTime.parse(location.timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        return newLocationTimeStamp.isAfter(lastLocationTimeStamp)
    }

    fun cancelLocationUpdates() {
        executor.shutdownNow()
    }

}