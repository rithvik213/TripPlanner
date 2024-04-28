package com.example.tripplanner

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.IOException
import java.util.Locale

class DiscoverPage : Fragment() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var tripAdvisorManager: TripAdvisorManager? = null
    private lateinit var attractionsAdapter: NearbyAttractionsAdapter
    private lateinit var attractionsRecyclerView: RecyclerView
    private lateinit var userLocationTextView: EditText


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
                    updateLocationName(location.latitude, location.longitude)
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

        userLocationTextView = view.findViewById(R.id.userlocation)

        arguments?.let {
            val userName = it.getString("userName", "User")
            view.findViewById<TextView>(R.id.userwelcome).text = "Welcome, $userName!"
        }

        tripAdvisorManager = TripAdvisorManager(requireContext(), cityName = "Current Location")

        attractionsRecyclerView = view.findViewById(R.id.nearbydestinationsrecycler)
        attractionsRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        attractionsAdapter = NearbyAttractionsAdapter(listOf())
        attractionsRecyclerView.adapter = attractionsAdapter

        requestLocationPermission()
        return view
    }

    private fun updateLocationName(latitude: Double, longitude: Double) {
        if (!Geocoder.isPresent()) {
            Log.w("DiscoverPage", "Geocoder not available")
            return
        }

        val geocoder = context?.let { Geocoder(it, Locale.getDefault()) }
        try {
            val addresses = geocoder?.getFromLocation(latitude, longitude, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                val city = address.locality ?: address.subAdminArea ?: "Unknown Location"
                userLocationTextView.setText(city)
            } else {
                userLocationTextView.setText("Unknown Location")
            }
        } catch (e: IOException) {
            Log.e("DiscoverPage", "Geocoder failed", e)
            userLocationTextView.setText("Failed to determine location")
        }
    }


    private fun fetchNearbyAttractions(latitude: Double, longitude: Double) {
        val latLong = "$latitude,$longitude"
        val attractionImageMap = mutableMapOf<String, String>()
        var imageFetchCount = 0

        tripAdvisorManager = TripAdvisorManager(requireContext(), cityName = "Current Location",
            object : TripAdvisorManager.AttractionFetchListener {
                override fun onAttractionsFetched(attractions: List<String>) {
                    if (attractions.isEmpty()) {
                        updateUI(listOf())
                    } else {
                        imageFetchCount = attractions.size
                        attractions.forEach { attraction ->
                            tripAdvisorManager?.fetchImage(attraction)
                            attractionImageMap[attraction] = "default_image_url"
                        }
                    }
                }

                override fun onAttractionFetchFailed(errorMessage: String) {
                    Log.d("DiscoveryPage", "Error fetching attractions: $errorMessage")
                    updateUI(listOf())
                }
            },
            object : TripAdvisorManager.ImageFetchListener {
                override fun onImageFetched(attraction: String, imageUrl: String) {
                    if (imageUrl.isNotEmpty()) {
                        attractionImageMap[attraction] = imageUrl
                    } else {
                        Log.d("DiscoveryPage", "No image available for $attraction")
                    }
                    imageFetchCount--
                    if (imageFetchCount <= 0) {
                        updateUIWithAttractions(attractionImageMap)
                    }
                }

                override fun onImageFetchFailed(attraction: String, errorMessage: String) {
                    Log.e("DiscoveryPage", "Failed to fetch image for $attraction: $errorMessage")
                    imageFetchCount--
                    if (imageFetchCount <= 0) {
                        updateUIWithAttractions(attractionImageMap)
                    }
                }
            }

        )
        tripAdvisorManager!!.fetchSearchTheLocation(category = "attractions", latLong = latLong, searchQuery = "things to do near me")
    }

    private fun updateUIWithAttractions(attractionImageMap: Map<String, String>) {
        val attractionObjects = attractionImageMap.map { (name, imageUrl) ->
            Attraction(name, imageUrl)
        }
        updateUI(attractionObjects)
    }

    private fun updateUI(attractions: List<Attraction>) {
        attractionsAdapter.updateData(attractions)
        Log.d("DiscoveryPage", "UI updated with all fetched attractions and images.")
        attractionsAdapter.notifyDataSetChanged()
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
            numUpdates = 1
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
        }
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback) // Ensure location updates are stopped when not needed
    }
}