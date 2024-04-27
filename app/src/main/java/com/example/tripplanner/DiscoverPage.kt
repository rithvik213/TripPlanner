package com.example.tripplanner

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult

class DiscoverPage : Fragment() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var tripAdvisorManager: TripAdvisorManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult) // Call the superclass method
                if (locationResult.locations.isNotEmpty()) {
                    val location = locationResult.locations[0] // Assuming first is the most accurate/newest
                    val latitude = location.latitude
                    val longitude = location.longitude
                    fetchNearbyAttractions(latitude, longitude)
                    fusedLocationClient.removeLocationUpdates(this) // Stop updates after receiving first location
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_discover_page, container, false)
        tripAdvisorManager = TripAdvisorManager(requireContext(), cityName = "Current Location")
        requestLocationPermission()
        return view
    }

    private fun fetchNearbyAttractions(latitude: Double, longitude: Double) {
        val latLong = "$latitude,$longitude"
        tripAdvisorManager = TripAdvisorManager(requireContext(), cityName = "Current Location",
            object : TripAdvisorManager.AttractionFetchListener {
                override fun onAttractionsFetched(attractions: List<String>) {
                    Log.d("DiscoverPage", "Fetched Attractions: $attractions")
                }

                override fun onAttractionFetchFailed(errorMessage: String) {
                    Log.e("DiscoverPage", "Error fetching attractions: $errorMessage")
                }
            })
        tripAdvisorManager?.fetchSearchTheLocation(category = "attractions", latLong = latLong, searchQuery = "things to do near me")
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1000)
        } else {
            startLocationUpdates()
        }
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create()?.apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            numUpdates = 1  // Request only a single update
        }
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (locationRequest != null) {
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1000 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates() // Start location updates only if permission is granted
        } else {
            // Handle the case where the user denies the permission
        }
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback) // Ensure location updates are stopped when not needed
    }
}
