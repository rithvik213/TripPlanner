package com.example.tripplanner

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.annotation.RequiresApi
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
    private lateinit var mapView: MapView
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
        mapView = view.findViewById(R.id.map)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        return view
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        if (mapView.viewTreeObserver.isAlive) {
            mapView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    mapView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    plotTrips()
                }
            })
        }
    }


    private fun plotTrips() {
        uiScope.launch {
            val account = userViewModel.getGoogleAccount()
            if (account != null) {
                val userId = account.id ?: return@launch
                val itineraries = withContext(Dispatchers.IO) {
                    appDatabase.itineraryDao().getItinerariesByUser(userId)
                }
                if (itineraries.isNotEmpty()) {
                    val boundsBuilder = LatLngBounds.Builder()
                    itineraries.forEach { itinerary ->
                        val parts = itinerary.latLong.split(", ")
                        val latitude = parts[0].toDouble()
                        val longitude = parts[1].toDouble()
                        val latLng = LatLng(latitude, longitude)
                        googleMap.addMarker(MarkerOptions()
                            .position(latLng)
                            .title(itinerary.cityName))
                        boundsBuilder.include(latLng)
                    }
                    val bounds = boundsBuilder.build()
                    val width = mapView.width
                    val height = mapView.height
                    val padding = 200
                    if (width > 0 && height > 0) {
                        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding)
                        googleMap.animateCamera(cameraUpdate)
                    }
                }
            } else {
                Toast.makeText(context, "User not logged in", Toast.LENGTH_LONG).show()
            }
        }
    }


    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onStop() {
        mapView.onStop()
        super.onStop()
    }

    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
        job.cancel()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}
