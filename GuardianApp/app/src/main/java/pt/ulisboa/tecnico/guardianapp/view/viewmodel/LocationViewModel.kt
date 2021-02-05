package pt.ulisboa.tecnico.guardianapp.view.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import pt.ulisboa.tecnico.guardianapp.model.Location
import pt.ulisboa.tecnico.guardianapp.repository.LocationRepository

class LocationViewModel : ViewModel() {

    fun getLastLocation(): LiveData<Location?> {
        return LocationRepository.getLastLocation()
    }

    fun getLocationHistory(): LiveData<List<Location>> {
        return LocationRepository.getLocationHistory()
    }

    fun enableLocationUpdates(interval: Long) = LocationRepository.enableLocationUpdates(interval)

    fun cancelLocationUpdates() = LocationRepository.cancelLocationUpdates()
}