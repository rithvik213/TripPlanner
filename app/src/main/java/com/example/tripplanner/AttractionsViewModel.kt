package com.example.tripplanner

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import com.example.tripplanner.apis.tripadvisor.TripAdvisorManager

class AttractionsViewModel : ViewModel() {
    private var tripAdvisorManager: TripAdvisorManager = TripAdvisorManager()

    private val _attractions = MutableLiveData<List<TripAdvisorManager.AttractionDetail>>()
    val attractions: LiveData<List<TripAdvisorManager.AttractionDetail>> = _attractions

    // Cache to store fetched attractions based on latitude and longitude keys
    private val attractionsCache = mutableMapOf<String, List<TripAdvisorManager.AttractionDetail>>()

    // Assuming attractionDetailsCache is defined somewhere in your ViewModel
    private val attractionDetailsCache = mutableMapOf<String, TripAdvisorManager.AttractionDetailsResponse>()

    // LiveData for UI updates if needed
    private val _attractionDetails = MutableLiveData<TripAdvisorManager.AttractionDetailsResponse>()
    val attractionDetails: LiveData<TripAdvisorManager.AttractionDetailsResponse> = _attractionDetails

    private var _currentCity = MutableLiveData<String?>()
    val currentCity: LiveData<String?> = _currentCity

    val userName = MutableLiveData<String>()

    var lastLocation: android.location.Location? = null

    val isLoading = MutableLiveData<Boolean>()



    fun initializeManager(context: Context) {
        tripAdvisorManager = TripAdvisorManager()
    }


    init {
        Log.d("AttractionsViewModel", "ViewModel instance created: $this")
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("AttractionsViewModel", "ViewModel instance cleared: $this")
    }

    fun updateLocation(context: Context, latitude: Double, longitude: Double) {
        val newLocation = android.location.Location("").apply {
            this.latitude = latitude
            this.longitude = longitude
        }
        if (lastLocation == null || newLocation.distanceTo(lastLocation!!) > 500) {
            lastLocation = newLocation
            isLoading.value = true
            fetchNearbyAttractions(context, latitude, longitude)
            _currentCity.postValue(null)
        }
    }

    fun shouldUpdateCityName(): Boolean {
        return currentCity.value.isNullOrEmpty()
    }



    fun fetchNearbyAttractions(context: Context, latitude: Double, longitude: Double) {
        val key = "$latitude,$longitude"
        val searchQuery = "things to do near me"

        Log.d("AttractionsViewModel", "Fetching attractions for key: $key")

        attractionsCache[key]?.let {
            Log.d("AttractionsViewModel", "Using cached data for key: $key")
            _attractions.postValue(it)
            isLoading.value = false
        } ?: run {
            Log.d("AttractionsViewModel", "No cache found for key: $key, fetching from API")
            tripAdvisorManager.fetchSearchTheLocation(
                context,
                "Current Location",
                "attractions",
                key,
                searchQuery,
                object : TripAdvisorManager.AttractionFetchListener {
                    override fun onAttractionsFetched(attractions: List<TripAdvisorManager.AttractionDetail>) {
                        if (attractions.isNotEmpty()) {
                            Log.d(
                                "AttractionsViewModel",
                                "Attractions fetched successfully: ${attractions.size}"
                            )
                            attractionsCache[key] = attractions
                            logAttractionsCache()
                            _attractions.postValue(attractions)
                        } else {
                            Log.d("AttractionsViewModel", "No attractions returned from API")
                            _attractions.postValue(listOf())
                        }
                        isLoading.value = false
                    }

                    override fun onAttractionFetchFailed(errorMessage: String) {
                        Log.e("AttractionsViewModel", "Error fetching attractions: $errorMessage")
                        _attractions.postValue(listOf())
                        isLoading.value = false
                    }
                })
        }
    }

    fun updateCurrentCity(cityName: String) {
        Log.d("AttractionsViewModel", "Updating current city to: $cityName")
        _currentCity.postValue(cityName)
        isLoading.value = false

    }



    // Fetch and cache attraction details
    fun fetchAttractionDetails(context: Context, locationId: String) {
        isLoading.value = true

        // Check if details are already cached
        attractionDetailsCache[locationId]?.let { cachedDetails ->
            _attractionDetails.postValue(cachedDetails) // Post value to LiveData if cached
            isLoading.value = false
            return
        }

        // Define the listener for the fetch call
        val detailListener = object : TripAdvisorManager.DetailFetchListener {
            override fun onDetailsFetched(detail: TripAdvisorManager.AttractionDetailsResponse) {
                // Cache the fetched details
                attractionDetailsCache[locationId] = detail
                // Post fetched details to LiveData
                _attractionDetails.postValue(detail)
                isLoading.value = false
            }

            override fun onDetailsFetchFailed(errorMessage: String) {
                Log.e("AttractionsViewModel", "Error fetching attraction details: $errorMessage")
                isLoading.value = false
                // Handle error appropriately, potentially notifying the UI
            }
        }

        // Call fetchAttractionDetails with the required parameters
        tripAdvisorManager.fetchAttractionDetails(context, locationId, detailListener)
    }

    // Get cached attraction details
    fun getCachedAttractionDetails(locationId: String): TripAdvisorManager.AttractionDetailsResponse? = attractionDetailsCache[locationId]

    fun getAttractionImage(locationId: String): String? {
        Log.d("AttractionsViewModel", "Attempting to find image URL for location ID: $locationId")

        if (attractionsCache.isEmpty()) {
            Log.d("AttractionsViewModel", "Attractions cache is currently empty")
        }

        attractionsCache.values.forEach { list ->
            list.find { it.locationID == locationId }?.let {
                Log.d("AttractionsViewModel", "Image URL found: ${it.imageUrl} for location ID: $locationId")
                return it.imageUrl
            }
        }

        Log.d("AttractionsViewModel", "No image URL found for location ID: $locationId")
        logAttractionsCache()
        return null
    }


    fun logAttractionsCache() {
        attractionsCache.forEach { (key, attractions) ->
            Log.d("AttractionsCache", "Key: $key")
            attractions.forEach { attraction ->
                Log.d("AttractionsCache", "Attraction ID: ${attraction.locationID}, Name: ${attraction.name}, Image URL: ${attraction.imageUrl}")
            }
        }
    }




}
