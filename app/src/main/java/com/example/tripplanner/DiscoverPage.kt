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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

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

        val destinationsrecyclerView: RecyclerView = view.findViewById(R.id.recyclerviewdestinations)
        val attractionsrecyclerView: RecyclerView = view.findViewById(R.id.nearbydestinationsrecycler)

        // Set LinearLayoutManager with horizontal orientation
        val attractionsLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        val destinationslayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        destinationsrecyclerView.layoutManager = destinationslayoutManager
        attractionsrecyclerView.layoutManager = attractionsLayoutManager


        val destinations = getDestinations()
        val attractions = getAttractions()
        destinationsrecyclerView.adapter = PopularDestinationsAdapter(destinations)
        attractionsrecyclerView.adapter = NearbyAttractionsAdapter(attractions)
        return view
    }

    private fun getAttractions(): List<Attraction> {
        // TO DO
        return listOf(
            Attraction(
                title = "Attraction",
                imageUrl = null
            ),
            Attraction(
                title = "Birthday",
                imageUrl = null
            )

        )
    }


    private fun getDestinations(): List<Destination> {
        return listOf(
            Destination(
                title = "New York City",
                imageUrl = "https://images.unsplash.com/photo-1546436836-07a91091f160?q=80&w=2948&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
            ),
            Destination(
                title = "Paris",
                imageUrl = "https://images.unsplash.com/photo-1522093007474-d86e9bf7ba6f?q=80&w=1600&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
            ),
            Destination(
                title = "Tokyo",
                imageUrl = "https://images.unsplash.com/photo-1545569341-9eb8b30979d9?q=80&w=2940&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
            ),
            Destination(
                title = "London",
                imageUrl = "https://images.unsplash.com/photo-1529655683826-aba9b3e77383?q=80&w=2865&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
            ),
            Destination(
                title = "Sydney",
                imageUrl = "https://images.unsplash.com/photo-1523428096881-5bd79d043006?q=80&w=2940&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
            ),
            Destination(
                title = "Los Angeles",
                imageUrl = "https://images.unsplash.com/flagged/photo-1575555201693-7cd442b8023f?q=80&w=3000&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
            ),
            Destination(
                title = "Rome",
                imageUrl = "https://images.unsplash.com/photo-1491566102020-21838225c3c8?q=80&w=3000&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
            ),
            Destination(
                title = "Beijing",
                imageUrl = "https://images.unsplash.com/photo-1590301729964-23833732ee04?q=80&w=2940&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
            ),
            Destination(
                title = "Mumbai",
                imageUrl = "https://images.unsplash.com/photo-1570168007204-dfb528c6958f?q=80&w=2835&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
            )

        )
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
