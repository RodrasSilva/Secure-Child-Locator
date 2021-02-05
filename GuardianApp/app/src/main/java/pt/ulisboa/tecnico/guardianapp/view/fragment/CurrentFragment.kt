package pt.ulisboa.tecnico.guardianapp.view.fragment

import android.content.SharedPreferences
import android.graphics.Camera
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import pt.ulisboa.tecnico.guardianapp.R
import pt.ulisboa.tecnico.guardianapp.view.viewmodel.LocationViewModel

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class CurrentFragment : Fragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    private val zoomLevel = 17f

    private lateinit var sharedPreferences: SharedPreferences

    private var isPaired = false
    private var googleMap: GoogleMap? = null
    private var radius: Double = 0.0
    private var circle: Circle? = null
    private var marker: Marker? = null

    private var interval: Long = 0
    private val locationViewModel: LocationViewModel by viewModels()
    private var lastLocationLatLng: LatLng? = null

    private val callback = OnMapReadyCallback { googleMap ->
        this.googleMap = googleMap
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        val fence = sharedPreferences.getString("fence", null)
        if (fence != null) {
            val coordinates = fence.split(" ")
            circle = googleMap.addCircle(
                CircleOptions()
                    .center(LatLng(coordinates[0].toDouble(), coordinates[1].toDouble()))
                    .radius(radius)
                    .strokeColor(Color.RED)
                    .strokeWidth(5f)
                    .strokePattern(listOf(Dash(30f), Gap(20f))))
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_current, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        radius = sharedPreferences.getString("radius", getString(R.string.radius_default))!!.toDouble()
        interval = sharedPreferences.getString("interval)", getString(R.string.location_updates_default))!!.toLong()
        isPaired = sharedPreferences.getBoolean("isPaired", false)

        val fab = view.findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            if (lastLocationLatLng != null)
                googleMap?.moveCamera(CameraUpdateFactory.newLatLng(lastLocationLatLng))
        }
        val mapFragment = childFragmentManager.findFragmentById(R.id.current_map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    override fun onResume() {
        super.onResume()
        if (isPaired) {
            getLocation()
            locationViewModel.enableLocationUpdates(interval)
        }
    }

    override fun onPause() {
        if (isPaired)
            locationViewModel.cancelLocationUpdates()
        super.onPause()
    }

    override fun onDestroy() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences?, key: String?) {
        when(key) {
            "radius" ->  {
                radius = prefs?.getString(key, getString(R.string.radius_default))!!.toDouble()
                if (circle != null) circle!!.radius = radius
            }
            "fence" -> {
                val fence = prefs?.getString(key, null)
                if (fence == null) {
                    circle?.remove()
                    circle = null
                } else {
                    val coordinates = fence.split(" ")
                    val latLng = LatLng(coordinates[0].toDouble(), coordinates[1].toDouble())
                    if (circle != null)
                        circle!!.center = latLng
                    else
                        circle = googleMap?.addCircle(
                            CircleOptions()
                                .center(LatLng(coordinates[0].toDouble(), coordinates[1].toDouble()))
                                .radius(radius)
                                .strokeColor(Color.RED)
                                .strokeWidth(5f)
                                .strokePattern(listOf(Dash(30f), Gap(20f))))
                }
            }
            "interval" -> {
                interval = prefs?.getString(key, getString(R.string.location_updates_default))!!.toLong()
            }
            "isPaired" -> isPaired = prefs?.getBoolean(key, false) ?: false
        }
    }

    private fun getLocation() {
        locationViewModel.getLastLocation().observe(this@CurrentFragment, Observer {
            if (it != null) {
                val latLng = LatLng(it.latitude, it.longitude)
                var isSafe = true
                if (circle != null) {
                    val distance = FloatArray(1)
                    Location.distanceBetween(
                        it.latitude,
                        it.longitude,
                        circle!!.center.latitude,
                        circle!!.center.longitude,
                        distance)
                    if (distance[0] > circle!!.radius) {
                        isSafe = false
                        Snackbar.make(requireView(), "The child has left the safe zone!",
                            Snackbar.LENGTH_INDEFINITE)
                            .setAction("OK") { }
                            .show()
                    }
                }
                if (marker == null) {
                    marker = googleMap?.addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .title("At ${it.timestamp}")
                            .icon(BitmapDescriptorFactory.defaultMarker(
                                if (isSafe)
                                    BitmapDescriptorFactory.HUE_GREEN
                                else
                                    BitmapDescriptorFactory.HUE_RED)))
                }
                else {
                    marker?.position = latLng
                    marker?.setIcon(BitmapDescriptorFactory.defaultMarker(
                        if (isSafe)
                            BitmapDescriptorFactory.HUE_GREEN
                        else
                            BitmapDescriptorFactory.HUE_RED))
                }
                if (lastLocationLatLng == null)
                    googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel))
                lastLocationLatLng = latLng
            }
        })
    }
}