package com.example.tripplanner
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.util.Log
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import java.io.IOException
import java.util.Locale

class DiscoverPage : Fragment() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var tripAdvisorManager: TripAdvisorManager? = null
    private lateinit var viewModel: AttractionsViewModel
    private lateinit var attractionsAdapter: NearbyAttractionsAdapter
    private lateinit var attractionsRecyclerView: RecyclerView
    private lateinit var userLocationTextView: EditText
    private var loadingDialog: AlertDialog? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        viewModel = ViewModelProvider(requireActivity()).get(AttractionsViewModel::class.java)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                if (locationResult.locations.isNotEmpty()) {
                    val location = locationResult.locations[0]
                    updateLocationName(location.latitude, location.longitude)
                    viewModel.fetchNearbyAttractions(requireContext(), location.latitude, location.longitude)
                    fusedLocationClient.removeLocationUpdates(this)
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
        val userIcon = view.findViewById<ImageButton>(R.id.usericon)
        userIcon.setOnClickListener {
            findNavController().navigate(R.id.action_discoverPage_to_userprofilefragment)
        }
        arguments?.let {
            val userName = it.getString("userName", "User")
            view.findViewById<TextView>(R.id.userwelcome).text = "Welcome, $userName!"
        }

        val destinationsrecyclerView: RecyclerView = view.findViewById(R.id.recyclerviewdestinations)

        attractionsRecyclerView = view.findViewById(R.id.nearbydestinationsrecycler)
        attractionsRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        attractionsAdapter = NearbyAttractionsAdapter(listOf()) { position ->
            val attractionId = attractionsAdapter.items[position].locationID
            val bundle = Bundle()
            bundle.putString("locationID", attractionId)
            findNavController().navigate(R.id.action_discoverPageFragment_to_AttractionsFragment, bundle)
        }

        attractionsRecyclerView.adapter = attractionsAdapter
        val destinationslayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        destinationsrecyclerView.layoutManager = destinationslayoutManager


        val destinations = getDestinations()

        viewModel.attractions.observe(viewLifecycleOwner) { attractions ->
            if (attractions.isNotEmpty()) {
                attractionsAdapter.updateData(attractions)
            } else {
                Log.d("DiscoverPage", "No attractions to display")
            }
        }

        val destinationsAdapter = PopularDestinationsAdapter(destinations) { destination ->
            val bundle = Bundle().apply {
                putString("destinationTitle", destination.title)
                putString("destinationImageURL", destination.imageUrl)
                putString("destinationDescription", destination.description)
            }
            findNavController().navigate(R.id.action_discoverPage_to_destinationDetailsFragment, bundle)
        }
        destinationsrecyclerView.adapter = destinationsAdapter

        requestLocationPermission()
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // Inflate the custom dialog layout
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog, null)
        //val userPromptEditText: EditText = dialogView.findViewById<EditText>(R.id.userpromptname)

        // Set the custom view to the dialog builder
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()  // Create the AlertDialog instance







        // Show the dialog
        dialog.show()
    }

    fun performOkAction() {
        //to do
    }

    private fun showLoadingDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        builder.setView(inflater.inflate(R.layout.loading_dialog, null))
        builder.setCancelable(false)  // Make it non-cancellable, so it doesn't dismiss on back press
        loadingDialog = builder.create()
        loadingDialog?.show()
    }

    private fun hideLoadingDialog() {
        loadingDialog?.dismiss()
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


    private fun updateUI(attractions: List<TripAdvisorManager.AttractionDetail>) {
        // Directly update the UI component, such as a RecyclerView adapter or similar
        attractionsAdapter.updateData(attractions)
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
                title = "New York City",
                imageUrl = "https://images.unsplash.com/photo-1544111795-fe8b9def73f6?q=80&w=2539&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
                description = "Experience the relentless energy and iconic sights of New York City, where dreams are made and pursued on the bustling streets of the world's most famous metropolis. From the dazzling lights of Times Square to the serene paths of Central Park, the city offers a dazzling array of experiences, cultures, and cuisines that captivate visitors from around the globe."
            ),
            Destination(
                title = "Paris",
                imageUrl = "https://images.unsplash.com/photo-1522093007474-d86e9bf7ba6f?q=80&w=1600&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
                description  = "Paris, the city of light and love, beckons with its unparalleled museums, charming street cafes, and exquisite cuisine. Stroll along the Seine, visit the majestic Eiffel Tower, or lose yourself in the art-filled corridors of the Louvre. Paris promises a magical experience infused with romance and beauty at every corner."
                ),
            Destination(
                title = "Tokyo",
                imageUrl = "https://images.unsplash.com/photo-1528164344705-47542687000d?q=80&w=2984&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
                description = "Dive into the vibrant heart of Tokyo, where ancient traditions blend seamlessly with cutting-edge technology. This bustling capital features neon-lit skyscrapers, historic temples, and bustling markets, offering a unique blend of the old and the new that enchants both the seasoned traveler and the curious explorer."
                ),
            Destination(
                title = "Sydney",
                imageUrl = "https://images.unsplash.com/photo-1523428096881-5bd79d043006?q=80&w=2940&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
                description = "Sydney, a bustling harbor city, is famed for its stunning coastline and vibrant culture. From the iconic Sydney Opera House to the rugged cliffs of the Blue Mountains, this city combines natural beauty with exuberant city life, offering endless opportunities for adventure and exploration."
                ),
            Destination(
                title = "Los Angeles",
                imageUrl = "https://images.unsplash.com/photo-1542737579-ba0a385f3b84?q=80&w=3088&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
                description = "Explore Los Angeles, a sun-soaked paradise where dreams of fame and fortune fill the balmy air. Home to Hollywood, expansive beaches, and an eclectic cultural scene, LA invites you to discover its storied boulevards, indulge in diverse cuisines, and experience its dynamic arts and entertainment offerings."
            ),
            Destination(
                title = "Beijing",
                imageUrl = "https://images.unsplash.com/photo-1584872589930-e99fe5bf4408?q=80&w=2954&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
                description = "Beijing stands as a majestic blend of ancient majesty and contemporary vigor. Home to imperial wonders such as the Forbidden City and the Temple of Heaven, Beijing also serves as a gateway to the Great Wall of China. This city is a profound tapestry of history interwoven with modernity, offering deep cultural experiences against a backdrop of rapid urban development."
                ),
            Destination(
                title = "Mumbai",
                imageUrl = "https://images.unsplash.com/photo-1529253355930-ddbe423a2ac7?q=80&w=2665&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
                description = "Step into the energetic pulse of Mumbai, a city of stark contrasts where glamour and tradition coexist. Known as the heart of the Bollywood film industry, Mumbai is also a place of historic markets, iconic architecture, and endless streets filled with the aroma of spicy street food."
                )

        )
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback) // Ensure location updates are stopped when not needed
    }
}