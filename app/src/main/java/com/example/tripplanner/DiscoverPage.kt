package com.example.tripplanner

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.tripplanner.apis.amadeus.data.Location
import com.example.tripplanner.apis.tripadvisor.TripAdvisorManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.*
import java.io.IOException
import java.util.Locale

class DiscoverPage : Fragment() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var viewModel: AttractionsViewModel
    private lateinit var userLocationTextView: TextView
    private var loadingDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeLocationComponents()
        Log.d("DiscoverPage", "Requesting location update onCreate")
        checkPermissionsAndStartLocationUpdates()
    }

    private fun initializeLocationComponents() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        viewModel = ViewModelProvider(requireActivity()).get(AttractionsViewModel::class.java)
        setupLocationCallback()
    }

    private fun checkPermissionsAndStartLocationUpdates() {
        if (checkPermissions()) {
            startLocationUpdates()
        } else {
            requestLocationPermission()
        }
    }

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (locationResult.locations.isEmpty()) {
                    Log.d("DiscoverPage", "No location data received.")
                    return
                }
                Log.d("DiscoverPage", "Location result received.")
                locationResult.locations.firstOrNull()?.let { location ->
                    Log.d("DiscoverPage", "Handling new location: Lat ${location.latitude}, Lon ${location.longitude}")
                    handleNewLocation(location)
                }
            }
        }
    }



    private fun handleNewLocation(location: android.location.Location) {
        viewModel.updateLocation(requireContext(), location.latitude, location.longitude)
        updateLocationName(location.latitude, location.longitude)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_discover_page, container, false).also { view ->
            setupUI(view)
        }
    }

    private fun setupUI(view: View) {
        userLocationTextView = view.findViewById(R.id.userlocation)
        view.findViewById<ImageButton>(R.id.usericon).setOnClickListener {
            findNavController().navigate(R.id.action_discoverPage_to_userprofilefragment)
        }

        val updateUserLocationButton = view.findViewById<ImageButton>(R.id.updateUserLocation)
        updateUserLocationButton.setOnClickListener {
            Log.d("DiscoverPage", "ImageButton clicked")
            if (checkPermissions()) {
                Log.d("DiscoverPage", "Permissions are granted, restarting location updates.")
                fusedLocationClient.removeLocationUpdates(locationCallback)  // Ensure old updates are removed
                startLocationUpdates()  // Start fresh location updates
            } else {
                Log.d("DiscoverPage", "Permissions not granted, requesting permissions.")
                requestLocationPermission()
            }
        }

        setupRecyclerViews(view)
        observeViewModel()
    }

    private fun setupRecyclerViews(view: View) {
        val attractionsRecyclerView = view.findViewById<RecyclerView>(R.id.nearbydestinationsrecycler).apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = NearbyAttractionsAdapter(listOf()) { position ->
                val attractionId = (adapter as NearbyAttractionsAdapter).items[position].locationID
                val bundle = Bundle().apply { putString("locationID", attractionId) }
                findNavController().navigate(R.id.action_discoverPageFragment_to_AttractionsFragment, bundle)
            }
        }
        val destinationsRecyclerView = view.findViewById<RecyclerView>(R.id.recyclerviewdestinations).apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = PopularDestinationsAdapter(getDestinations()) { destination ->
                val bundle = Bundle().apply {
                    putString("destinationTitle", destination.title)
                    putString("destinationImageURL", destination.imageUrl)
                    putString("destinationDescription", destination.description)
                }
                findNavController().navigate(R.id.action_discoverPage_to_destinationDetailsFragment, bundle)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            if (loading) {
                showLoadingDialog()
            } else {
                dismissLoadingDialog()
            }
        }

        viewModel.userName.observe(viewLifecycleOwner) { userName ->
            val welcomeText = if (userName.isNullOrEmpty()) "Welcome, User" else "Welcome, $userName"
            view?.findViewById<TextView>(R.id.userwelcome)?.text = welcomeText
        }

        if (viewModel.userName.value == null) {
            arguments?.getString("userName", "User")?.let { userName ->
                viewModel.userName.value = userName
            }
        }
        viewModel.currentCity.observe(viewLifecycleOwner) { cityName ->
            val locationText = cityName ?: "Determining Location..."
            Log.d("DiscoverPage", "Current city updated in UI: $locationText")
            userLocationTextView.text = locationText
        }

        viewModel.attractions.observe(viewLifecycleOwner) { attractions ->
            (view?.findViewById<RecyclerView>(R.id.nearbydestinationsrecycler)?.adapter as? NearbyAttractionsAdapter)?.updateData(attractions)
        }

    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    fun performOkAction() {
        //to do
    }

    private fun updateLocationName(latitude: Double, longitude: Double) {
        if (!Geocoder.isPresent()) {
            Log.w("DiscoverPage", "Geocoder not available")
            userLocationTextView.text = "Geocoder not available"
            viewModel.updateCurrentCity("Geocoder not available")
            return
        }

        val geocoder = context?.let { Geocoder(it, Locale.getDefault()) }
        try {
            val addresses = geocoder?.getFromLocation(latitude, longitude, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                val city = address.locality ?: address.subAdminArea ?: "Unknown Location"
                Log.d("DiscoverPage", "Geocoding successful, city found: $city")
                viewModel.updateCurrentCity(city)
            } else {
                Log.e("DiscoverPage", "No address found, updating city as Unknown")
                viewModel.updateCurrentCity("Unknown Location")
            }
        } catch (e: IOException) {
            Log.e("DiscoverPage", "Geocoder failed", e)
            viewModel.updateCurrentCity("Failed to determine location")
        }
    }

    private fun showLoadingDialog() {
        if (loadingDialog == null) {
            loadingDialog = AlertDialog.Builder(context)
                .setView(LayoutInflater.from(context).inflate(R.layout.personalize_dialog, null))
                .setCancelable(false)
                .create()
        }
        loadingDialog?.show()
    }

    private fun dismissLoadingDialog() {
        loadingDialog?.dismiss()
    }




    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1000)
        } else {
            startLocationUpdates()
        }
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(10000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setMinUpdateIntervalMillis(5000)
            .setMaxUpdates(1)
            .build()

        fusedLocationClient.removeLocationUpdates(locationCallback)

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            viewModel.isLoadingGeo.value = true
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }

    private fun checkPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1000 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates()
        }
    }

    private fun getDestinations(): List<Destination> {
        return listOf(
            Destination(
                title = "London",
                imageUrl = "https://images.unsplash.com/photo-1529655683826-aba9b3e77383?q=80&w=2865&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
                description = "London, a city steeped in history yet fervently modern, invites you to wander through centuries-old streets that echo with tales of innovation and conquest. Explore world-class museums, royal parks, and landmarks like the Tower of London and Buckingham Palace, all set against a backdrop of dynamic cultural diversity."
            ),
            Destination(
                title = "Rome",
                imageUrl = "https://images.unsplash.com/photo-1542820229-081e0c12af0b?q=80&w=2946&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
                description = "Rome, the eternal city, offers a timeless journey through history, art, and culture. Wander among the ruins of the Roman Forum, gaze at the magnificence of the Colosseum, and step into the Vatican City to witness the splendor of St. Peter's Basilica. Rome's rich heritage and vibrant street life make every visit a mesmerizing encounter with the past."
            ),
            Destination(
                title = "Mumbai",
                imageUrl = "https://images.unsplash.com/photo-1523980077198-60824a7b2148?q=80&w=3087&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
                description = "Step into the energetic pulse of Mumbai, a city of stark contrasts where glamour and tradition coexist. Known as the heart of the Bollywood film industry, Mumbai is also a place of historic markets, iconic architecture, and endless streets filled with the aroma of spicy street food."
            ),
            Destination(
                title = "Paris",
                imageUrl = "https://images.unsplash.com/photo-1541628951107-a9af5346a3e4?q=80&w=3089&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
                description  = "Paris, the city of light and love, beckons with its unparalleled museums, charming street cafes, and exquisite cuisine. Stroll along the Seine, visit the majestic Eiffel Tower, or lose yourself in the art-filled corridors of the Louvre. Paris promises a magical experience infused with romance and beauty at every corner."
            ),
            Destination(
                title = "Tokyo",
                imageUrl = "https://images.unsplash.com/photo-1528164344705-47542687000d?q=80&w=2984&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
                description = "Dive into the vibrant heart of Tokyo, where ancient traditions blend seamlessly with cutting-edge technology. This bustling capital features neon-lit skyscrapers, historic temples, and bustling markets, offering a unique blend of the old and the new that enchants both the seasoned traveler and the curious explorer."
            ),
            Destination(
                title = "Sydney",
                imageUrl = "https://images.unsplash.com/photo-1494233892892-84542a694e72?q=80&w=2242&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
                description = "Sydney, a bustling harbor city, is famed for its stunning coastline and vibrant culture. From the iconic Sydney Opera House to the rugged cliffs of the Blue Mountains, this city combines natural beauty with exuberant city life, offering endless opportunities for adventure and exploration."
            ),
            Destination(
                title = "Beijing",
                imageUrl = "https://images.unsplash.com/photo-1516545595035-b494dd0161e4?q=80&w=2940&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
                description = "Beijing stands as a majestic blend of ancient majesty and contemporary vigor. Home to imperial wonders such as the Forbidden City and the Temple of Heaven, Beijing also serves as a gateway to the Great Wall of China. This city is a profound tapestry of history interwoven with modernity, offering deep cultural experiences against a backdrop of rapid urban development."
            ),
        )
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}
