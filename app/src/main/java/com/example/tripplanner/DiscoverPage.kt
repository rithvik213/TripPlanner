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
        destinationsrecyclerView.adapter = PopularDestinationsAdapter(destinations)

        viewModel.attractions.observe(viewLifecycleOwner) { attractions ->
            if (attractions.isNotEmpty()) {
                attractionsAdapter.updateData(attractions)
            } else {
                Log.d("DiscoverPage", "No attractions to display")
            }
        }


        requestLocationPermission()
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // Inflate the custom dialog layout
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog, null)
        val userPromptEditText: EditText = dialogView.findViewById<EditText>(R.id.userpromptname)

        // Set the custom view to the dialog builder
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()  // Create the AlertDialog instance

        // Find buttons within the custom layout
        val okButton = dialogView.findViewById<ImageButton>(R.id.dialogButtonOk)

        // Set click listeners for the buttons
        okButton.setOnClickListener {
            performOkAction()
            dialog.dismiss()  // Dismiss the dialog when OK button is clicked
        }



        // Show the dialog
        //dialog.show()
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

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback) // Ensure location updates are stopped when not needed
    }
}