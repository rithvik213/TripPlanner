package com.example.tripplanner

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.tripplanner.database.AppDatabase
import com.example.tripplanner.database.MyApp
import com.example.tripplanner.viewmodels.UserViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserProfile : Fragment(), OnMapReadyCallback {
    private var mapView: MapView? = null
    private lateinit var googleMap: GoogleMap
    private lateinit var userViewModel: UserViewModel
    private lateinit var appDatabase: AppDatabase

    private var job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val app = requireActivity().application as MyApp
        appDatabase = app.database
        userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
        val view = inflater.inflate(R.layout.fragment_user_profile, container, false)
        mapView = view.findViewById<MapView>(R.id.map)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)
        return view
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.clear()
        plotTrips()
    }

    private fun plotTrips() {
        //Do in coroutine to make sure main thread isn't bogged down by potentially heavy work
        uiScope.launch {
            val account = userViewModel.getGoogleAccount()

            //Make sure that the user's account isn't null
            if (account != null) {
                //Get the userid, and stop if it's null
                val userId = account.id ?: return@launch

                //Get the user's itineraries
                val itineraries = withContext(Dispatchers.IO) {
                    appDatabase.itineraryDao().getItinerariesByUser(userId)
                }

                //Check if the fragment is added to activity and if we have any itineraries
                if (isAdded && itineraries.isNotEmpty()) {
                    // Create a builder for LatLngBounds to encompass all markers
                    val boundsBuilder = LatLngBounds.Builder()

                    //For each itinerary, get the latlong coords and make an LatLng object
                    itineraries.forEach { itinerary ->
                        // Split the latitude and longitude from the "latLong" string
                        val parts = itinerary.latLong.split(", ")
                        val latitude = parts[0].toDouble()
                        val longitude = parts[1].toDouble()
                        val latLng = LatLng(latitude, longitude)

                        //Add a marker to the embedded google map at the latlong locations
                        googleMap.addMarker(MarkerOptions().position(latLng).title(itinerary.cityName))
                        boundsBuilder.include(latLng)
                    }

                    val bounds = boundsBuilder.build()
                    val width = mapView?.width ?: 0
                    val height = mapView?.height ?: 0
                    val padding = 200
                    if (width > 0 && height > 0) {
                        //Update the camera to move so it shows all the markers
                        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding)

                        //Then animate it camera to the bounds
                        googleMap.animateCamera(cameraUpdate)
                    }
                }
            } else {
                if (isAdded) {
                    Toast.makeText(context, "User not logged in", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    //Make sure to pause and stop the mapView as necessary since it doesn't do it on its own
    override fun onResume() {
        super.onResume()
        mapView?.onResume()
        plotTrips()
    }

    override fun onPause() {
        mapView?.onPause()
        super.onPause()
    }

    override fun onStop() {
        mapView?.onStop()
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView?.onDestroy()
        mapView = null
        job.cancel()
    }
}

