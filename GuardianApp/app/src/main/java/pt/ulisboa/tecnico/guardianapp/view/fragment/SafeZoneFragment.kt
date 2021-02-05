package pt.ulisboa.tecnico.guardianapp.view.fragment

import android.content.SharedPreferences
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import pt.ulisboa.tecnico.guardianapp.R
import pt.ulisboa.tecnico.guardianapp.view.viewmodel.LocationViewModel

class SafeZoneFragment : Fragment() {

    private val zoomLevel = 17f

    private lateinit var sharedPreferences: SharedPreferences
    private var radius: Double = 0.0
    private var location: LatLng? = null
    private var circle: Circle? = null

    private val locationViewModel: LocationViewModel by viewModels()
    private var googleMap: GoogleMap? = null
    private val callback = OnMapReadyCallback { googleMap ->
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        this.googleMap = googleMap
        googleMap.setOnMapClickListener {
            var create = true
            if (circle != null) {
                val floatArray = FloatArray(1)
                Location.distanceBetween(
                    it.latitude,
                    it.longitude,
                    circle!!.center.latitude,
                    circle!!.center.longitude,
                    floatArray)
                if (floatArray[0] <= radius) create = false
                circle!!.remove()
                circle = null
            }
            if (create) {
                circle = googleMap.addCircle(
                    CircleOptions()
                        .center(it)
                        .radius(radius)
                        .strokeColor(Color.RED)
                        .strokeWidth(5f)
                        .strokePattern(listOf<PatternItem>(Dash(30f), Gap(20f))))
                location = it
            }
        }

        val fence = sharedPreferences.getString("fence", null)
        if (fence != null) {
            val coordinates = fence.split(" ")
            circle = googleMap.addCircle(
                CircleOptions()
                    .center(LatLng(coordinates[0].toDouble(), coordinates[1].toDouble()))
                    .radius(radius)
                    .strokeColor(Color.RED)
                    .strokeWidth(5f)
                    .strokePattern(listOf<PatternItem>(Dash(30f), Gap(20f))))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_safe_zone, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        val mapFragment = childFragmentManager.findFragmentById(R.id.fence_map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        val doneBtn = view.findViewById(R.id.safeZoneBtn) as Button
        doneBtn.setOnClickListener {
            if (location != null)
                sharedPreferences.edit().putString("fence", "${location?.latitude} ${location?.longitude}").apply()
            else sharedPreferences.edit().remove("fence").apply()
            activity?.supportFragmentManager?.popBackStack()
        }

        radius = sharedPreferences.getString("radius", getString(R.string.radius_default))!!.toDouble()
    }

    override fun onResume() {
        super.onResume()
        if (sharedPreferences.getBoolean("isPaired", false))
            getLocation()
    }

    private fun getLocation() {
        locationViewModel.getLastLocation().observe(this@SafeZoneFragment, Observer {
            if (it != null) {
                val latLng = LatLng(it.latitude, it.longitude)
                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel))
            }
        })
    }
}