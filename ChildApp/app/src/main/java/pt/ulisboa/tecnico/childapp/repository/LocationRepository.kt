package pt.ulisboa.tecnico.childapp.repository

import android.location.Location
import pt.ulisboa.tecnico.childapp.model.MyLocation
import pt.ulisboa.tecnico.childapp.model.toSecureLocation
import pt.ulisboa.tecnico.childapp.repository.service.ServerApi
import java.time.LocalDateTime

object LocationRepository {

    fun addLocation(childId: String, location: Location): Boolean {
        val mLocation = MyLocation(location.latitude, location.longitude, LocalDateTime.now().toString())
        return ServerApi.addLocation(childId, mLocation.toSecureLocation())
    }
}