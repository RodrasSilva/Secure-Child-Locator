package pt.ulisboa.tecnico.childapp.service

import android.annotation.SuppressLint
import android.app.*
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.location.Location
import android.os.Binder
import android.os.IBinder
import android.os.Looper
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import com.google.android.gms.location.*
import pt.ulisboa.tecnico.childapp.App
import pt.ulisboa.tecnico.childapp.R
import pt.ulisboa.tecnico.childapp.repository.LocationRepository
import pt.ulisboa.tecnico.childapp.view.activity.MainActivity

private const val CHANNEL_ID = "channel_01"
private const val NOTIFICATION_ID = 12345678
private const val UPDATES_INTERVAL = 15_000L // ms

class LocationUpdatesService : Service() {
    companion object {
        private val PACKAGE_NAME = "pt.ulisboa.tecnico.childapp.service.locationupdatesservice"
        val ACTION_BROADCAST = "${PACKAGE_NAME}.broadcast"
        val EXTRA_LOCATION = "${PACKAGE_NAME}.location"
        val EXTRA_FROM_NOTIFICATION = "${PACKAGE_NAME}.from_notification"
    }

    inner class LocalBinder : Binder() {
        fun getService(): LocationUpdatesService = this@LocationUpdatesService
    }
    private val binder = LocalBinder()
    private var isChangingConfiguration = false;

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var notificationManager: NotificationManager

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult?) {
            super.onLocationResult(result)
            if (result != null) handleLocationUpdate(result.lastLocation)
        }
    }

    override fun onCreate() {
        super.onCreate()
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(applicationContext)
        locationRequest = LocationRequest()
            .setInterval(UPDATES_INTERVAL)
            .setFastestInterval(UPDATES_INTERVAL)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val name: CharSequence = getString(R.string.app_name)
        // Create the channel for the notification
        val mChannel = NotificationChannel(
            CHANNEL_ID,
            name,
            NotificationManager.IMPORTANCE_HIGH)
        // Set the Notification Channel for the Notification Manager.
        notificationManager.createNotificationChannel(mChannel)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val isFromNotification = intent?.getBooleanExtra(EXTRA_FROM_NOTIFICATION, false)
            ?: false
        if (isFromNotification) {
            removeLocationUpdates()
            stopSelf()
        }
        return START_NOT_STICKY
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        isChangingConfiguration = true
    }

    override fun onBind(intent: Intent?): IBinder? {
        stopForeground(true)
        isChangingConfiguration = false
        return binder
    }

    override fun onRebind(intent: Intent?) {
        stopForeground(true)
        isChangingConfiguration = false
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        if (!isChangingConfiguration)
            startForeground(NOTIFICATION_ID, getNotification())
        return true
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        startService(Intent(applicationContext, LocationUpdatesService::class.java))
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback,
            Looper.myLooper())
    }

    private fun removeLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    private fun handleLocationUpdate(location: Location) {
        val intent = Intent(ACTION_BROADCAST)
        intent.putExtra(EXTRA_LOCATION, location)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
        val isPaired = sharedPreferences.getBoolean("isPaired", false)
        if (isPaired)
            if (!LocationRepository.addLocation((applicationContext as App).uniqueId, location))
                Toast.makeText(applicationContext,
                    "Something went wrong while sending location updates!",
                    Toast.LENGTH_LONG).show()
    }

    private fun getNotification(): Notification {
        val intent = Intent(this, LocationUpdatesService::class.java)

        // Extra to help figure out if arrived in onStartCommand via the notification or not.
        intent.putExtra(EXTRA_FROM_NOTIFICATION, true)

        // The PendingIntent that leads to a call to onStartCommand() in this service.
        val servicePendingIntent = PendingIntent.getService(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        // The PendingIntent to launch activity.
        val activityPendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java), 0
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .addAction(R.drawable.ic_launch, getString(R.string.launch_activity),
                activityPendingIntent)
            .addAction(R.drawable.ic_cancel, getString(R.string.remove_location_updates),
                servicePendingIntent)
            .setContentTitle(getString(R.string.notification_text))
            .setOngoing(true)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setWhen(System.currentTimeMillis())
        return builder.build()
    }
}