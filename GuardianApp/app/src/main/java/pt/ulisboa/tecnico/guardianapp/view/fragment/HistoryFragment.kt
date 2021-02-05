package pt.ulisboa.tecnico.guardianapp.view.fragment


import android.content.SharedPreferences
import android.graphics.Camera
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import pt.ulisboa.tecnico.guardianapp.R
import pt.ulisboa.tecnico.guardianapp.view.viewmodel.LocationViewModel

class HistoryFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences

    private var googleMap: GoogleMap? = null
    private var movedCamera = false
    private val locationViewModel: LocationViewModel by viewModels()

    private val callback = OnMapReadyCallback { googleMap ->
        this.googleMap = googleMap
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)

        val mapFragment = childFragmentManager.findFragmentById(R.id.history_map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    override fun onResume() {
        super.onResume()
        if (sharedPreferences.getBoolean("isPaired", false))
            getLocationHistory()
    }

    private fun getLocationHistory() {
        locationViewModel.getLocationHistory().observe(this@HistoryFragment, Observer { locations ->
            locations.forEach {
                googleMap?.addMarker(
                    MarkerOptions()
                        .position(LatLng(it.latitude, it.longitude))
                        .title("At ${it.timestamp}")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))
                if (!movedCamera) {
                    val lastLocation = locations[locations.size-1]
                    googleMap?.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(lastLocation.latitude, lastLocation.longitude),
                            17f))
                    movedCamera = true
                }
            }
        })
    }
}