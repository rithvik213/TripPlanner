package com.example.tripplanner.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import com.example.tripplanner.apis.tripadvisor.TripAdvisorManager
import kotlinx.coroutines.launch

// Allows us to cache TripAdvisor calls for our home page
class AttractionsViewModel : ViewModel() {
    private var tripAdvisorManager: TripAdvisorManager = TripAdvisorManager()

    private val _attractions = MutableLiveData<List<TripAdvisorManager.AttractionDetail>>()
    val attractions: LiveData<List<TripAdvisorManager.AttractionDetail>> = _attractions

    //Cache to store fetched attractions based on latitude and longitude keys
    private val attractionsCache = mutableMapOf<String, List<TripAdvisorManager.AttractionDetail>>()

    private val attractionDetailsCache = mutableMapOf<String, TripAdvisorManager.AttractionDetailsResponse>()

    //LiveData for UI updates if needed
    private val _attractionDetails = MutableLiveData<TripAdvisorManager.AttractionDetailsResponse>()
    val attractionDetails: LiveData<TripAdvisorManager.AttractionDetailsResponse> = _attractionDetails

    private var _currentCity = MutableLiveData<String?>()
    val currentCity: LiveData<String?> = _currentCity

    val userName = MutableLiveData<String>()

    // Does not make subsequent calls unless we move enough
    var lastLocation: android.location.Location? = null

    var isLoadingGeo = MutableLiveData<Boolean>(false)
    private var isLoadingAttractions = MutableLiveData<Boolean>(false)

    val isLoading: LiveData<Boolean> = MediatorLiveData<Boolean>().apply {
        addSource(isLoadingGeo) { value = isLoadingGeo.value!! || isLoadingAttractions.value!! }
        addSource(isLoadingAttractions) { value = isLoadingGeo.value!! || isLoadingAttractions.value!! }
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
            isLoadingAttractions.value = true
            fetchNearbyAttractions(context, latitude, longitude)
            _currentCity.postValue(null)
        }
    }

    fun fetchNearbyAttractions(context: Context, latitude: Double, longitude: Double) {
        val key = "$latitude,$longitude"
        val searchQuery = "things to do near me"

        Log.d("AttractionsViewModel", "Fetching attractions for key: $key")

        viewModelScope.launch {
            isLoadingAttractions.value = true

            // Check cache first
            val cachedAttractions = attractionsCache[key]
            if (cachedAttractions != null) {
                Log.d("AttractionsViewModel", "Using cached data for key: $key")
                _attractions.postValue(cachedAttractions.filter { it.imageUrl?.isNotEmpty() == true }) // Post only attractions with images
                isLoadingAttractions.value = false
            } else {
                Log.d("AttractionsViewModel", "No cache found for key: $key, fetching from API")
                try {
                    val attractions = tripAdvisorManager.fetchSearchTheLocation(
                        "Current Location",
                        "attractions",
                        key,
                        searchQuery
                    )
                    if (attractions.isNotEmpty()) {
                        Log.d("AttractionsViewModel", "Attractions fetched successfully: ${attractions.size}")
                        val attractionsWithImages = attractions.filter { it.imageUrl?.isNotEmpty() == true }
                        attractionsCache[key] = attractionsWithImages // Cache the filtered list
                        _attractions.postValue(attractionsWithImages)
                    } else {
                        Log.d("AttractionsViewModel", "No attractions returned from API")
                        _attractions.postValue(listOf())
                    }
                } catch (e: Exception) {
                    Log.e("AttractionsViewModel", "Error fetching attractions: ${e.message}")
                    _attractions.postValue(listOf())
                } finally {
                    isLoadingAttractions.value = false
                }
            }
        }
    }


    /*
    fun fetchNearbyAttractions(context: Context, latitude: Double, longitude: Double) {
        val key = "$latitude,$longitude"
        val searchQuery = "things to do near me"

        Log.d("AttractionsViewModel", "Fetching attractions for key: $key")

        // Check cache first
        attractionsCache[key]?.let {
            Log.d("AttractionsViewModel", "Using cached data for key: $key")
            _attractions.postValue(it.filter { it.imageUrl?.isNotEmpty() == true}) // Post only attractions with images
            isLoadingAttractions.value = false
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
                            Log.d("AttractionsViewModel", "Attractions fetched successfully: ${attractions.size}")
                            val attractionsWithImages = attractions.filter { it.imageUrl?.isNotEmpty() == true }
                            attractionsCache[key] = attractionsWithImages // Cache the filtered list
                            logAttractionsCache()
                            _attractions.postValue(attractionsWithImages)
                        } else {
                            Log.d("AttractionsViewModel", "No attractions returned from API")
                            _attractions.postValue(listOf())
                        }
                        isLoadingAttractions.value = false
                    }

                    override fun onAttractionFetchFailed(errorMessage: String) {
                        Log.e("AttractionsViewModel", "Error fetching attractions: $errorMessage")
                        _attractions.postValue(listOf())
                        isLoadingAttractions.value = false
                    }
                })
        }
    }

     */


    fun updateCurrentCity(cityName: String) {
        Log.d("AttractionsViewModel", "Updating current city to: $cityName")
        _currentCity.postValue(cityName)
        isLoadingGeo.value = false

    }



    // Fetch and cache attraction details
    fun fetchAttractionDetails(context: Context, locationId: String) {


        // Check if details are already cached
        attractionDetailsCache[locationId]?.let { cachedDetails ->
            _attractionDetails.postValue(cachedDetails) // Post value to LiveData if cached
            isLoadingAttractions.value = false
            return
        }

        // Define the listener for the fetch call
        val detailListener = object : TripAdvisorManager.DetailFetchListener {
            override fun onDetailsFetched(detail: TripAdvisorManager.AttractionDetailsResponse) {
                // Cache the fetched details
                attractionDetailsCache[locationId] = detail
                // Post fetched details to LiveData
                _attractionDetails.postValue(detail)
                isLoadingAttractions.value = false
            }

            override fun onDetailsFetchFailed(errorMessage: String) {
                Log.e("AttractionsViewModel", "Error fetching attraction details: $errorMessage")
                isLoadingAttractions.value = false
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
