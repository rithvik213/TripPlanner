package com.example.tripplanner.apis.tripadvisor

import android.content.Context
import android.util.Log
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url

// Specifies the callbacks for TripAdvisor responses and the API interface
class TripAdvisorManager {

    interface AttractionFetchListener {
        fun onAttractionsFetched(attractions: List<AttractionDetail>)

        fun onAttractionFetchFailed(errorMessage: String)

    }

    interface ImageFetchListener {
        fun onImageFetched(attractionDetail: AttractionDetail)
        fun onImageFetchFailed(locationID: String, errorMessage: String)
    }

    interface DetailFetchListener {
        fun onDetailsFetched(detail: AttractionDetailsResponse)
        fun onDetailsFetchFailed(errorMessage: String)
    }

    private val apiKey = "TRIP_ADVISOR_KEY"
    private val baseUrl = "https://api.content.tripadvisor.com/api/v1/location/"

    // retrofit instance for location search endpoint
    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create(TripAdvisorService::class.java)

    // Fetches the images and the attraction names for a give city
    fun fetchData(context: Context, searchQuery: String, latLong: String?, listener: AttractionFetchListener) {
        fetchAttractions(context, searchQuery, latLong, object : AttractionFetchListener {
            override fun onAttractionsFetched(attractions: List<AttractionDetail>) {
                // Process each fetched attraction to get images
                attractions.forEach { attraction ->
                    fetchImage(attraction.locationID) { imageUrl ->
                        // Update the attraction with the fetched image URL and pass to listener
                        val updatedAttraction = attraction.copy(imageUrl = imageUrl)
                        listener.onAttractionsFetched(listOf(updatedAttraction))
                    }
                }
            }

            override fun onAttractionFetchFailed(errorMessage: String) {
                listener.onAttractionFetchFailed(errorMessage)
            }
        })
    }

    fun fetchCityImage(cityName: String, callback: (String) -> Unit) {
        fetchLocationId(cityName) { locationId ->
            if (locationId.isNotEmpty()) {
                fetchImage(locationId, callback)
            } else {
                callback("")
            }
        }
    }

    // Gets the first response to the location id endpoint to use for other endpoints in subsequent API calls
    private fun fetchLocationId(cityName: String, callback: (String) -> Unit) {
        val url = "search?searchQuery=$cityName&language=en&key=$apiKey"
        val call = service.searchLocations(url)
        call.enqueue(object : retrofit2.Callback<LocationSearchResponse> {
            override fun onResponse(call: Call<LocationSearchResponse>, response: retrofit2.Response<LocationSearchResponse>) {
                if (response.isSuccessful) {
                    val locationId = response.body()?.data?.firstOrNull()?.location_id ?: ""
                    callback(locationId)
                } else {
                    //Handle error, could log or display to user
                }
            }

            override fun onFailure(call: Call<LocationSearchResponse>, t: Throwable) {
                //Handle error, could log or display to user
            }
        })
    }

    // Gets the largest image for a location ID after the fetchLocationId call
    fun fetchImage(locationId: String, callback: ImageFetchCallback) {
        val url = "$locationId/photos?language=en&key=$apiKey"
        val call = service.getLocationPhotos(url)
        call.enqueue(object : retrofit2.Callback<PhotoResponse> {
            override fun onResponse(call: Call<PhotoResponse>, response: retrofit2.Response<PhotoResponse>) {
                val imageUrl = response.body()?.data?.firstOrNull()?.images?.original?.url.orEmpty()
                callback(imageUrl)
            }

            override fun onFailure(call: Call<PhotoResponse>, t: Throwable) {
                Log.e(
                    "TripAdvisorManager",
                    "Failed to fetch image for location ID $locationId: ${t.message}"
                )
                callback("")
            }
        })
    }

    fun fetchAttractions(context: Context, searchQuery: String, latLong: String?, listener: AttractionFetchListener) {
        // Constructing the URL to include necessary parameters
        val url = buildString {
            append("https://api.content.tripadvisor.com/api/v1/location/search?")
            append("searchQuery=${searchQuery.urlEncode()}")
            if (!latLong.isNullOrEmpty()) {
                append("&latLong=${latLong.urlEncode()}")
            }
            append("&language=en")
            append("&key=$apiKey")
        }

        val call = service.searchLocations(url)
        call.enqueue(object : retrofit2.Callback<LocationSearchResponse> {
            override fun onResponse(call: Call<LocationSearchResponse>, response: retrofit2.Response<LocationSearchResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val attractions = response.body()!!.data.map { locationData ->
                        AttractionDetail(
                            locationID = locationData.location_id,
                            name = locationData.name,
                            imageUrl = null
                        )
                    }
                    listener.onAttractionsFetched(attractions)
                } else {
                    listener.onAttractionFetchFailed("Error fetching data: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<LocationSearchResponse>, t: Throwable) {
                listener.onAttractionFetchFailed("Network error: ${t.message}")
            }
        })
    }


    // Gets images for our home screen based on our current location
    fun fetchSearchTheLocation(context: Context, cityName: String, category: String, latLong: String, searchQuery: String, listener: AttractionFetchListener) {
        val url = "search?key=$apiKey&searchQuery=${searchQuery.urlEncode()}&category=$category&latLong=$latLong&language=en"
        val call = service.searchLocations(baseUrl + url)
        call.enqueue(object : retrofit2.Callback<LocationSearchResponse> {
            val attractions = mutableMapOf<String, AttractionDetail>()
            var pendingImageFetches = 0

            override fun onResponse(call: Call<LocationSearchResponse>, response: retrofit2.Response<LocationSearchResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    response.body()!!.data.forEach { locationData ->
                        val attractionDetail = AttractionDetail(
                            locationID = locationData.location_id,
                            name = locationData.name,
                            imageUrl = null
                        )
                        attractions[locationData.location_id] = attractionDetail
                        pendingImageFetches++
                        fetchImage(locationData.location_id) { imageUrl ->
                            synchronized(this) {
                                attractions[locationData.location_id]?.imageUrl = imageUrl
                                pendingImageFetches--
                                Log.d(
                                    "TripAdvisorManager",
                                    "Fetched image for ${locationData.name}: $imageUrl"
                                )
                                if (pendingImageFetches == 0) {
                                    Log.d(
                                        "TripAdvisorManager",
                                        "All images fetched, updating listener."
                                    )
                                    listener?.onAttractionsFetched(attractions.values.toList())
                                }
                            }
                        }
                    }
                    if (attractions.isEmpty()) {
                        listener?.onAttractionsFetched(listOf()) // Handle case with no attractions
                    }
                } else {
                    listener?.onAttractionFetchFailed("Error fetching data: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<LocationSearchResponse>, t: Throwable) {
                synchronized(this) {
                    listener?.onAttractionFetchFailed("Network error: ${t.message}")
                    if (attractions.isEmpty() && pendingImageFetches == 0) {
                        listener?.onAttractionsFetched(listOf())
                    }
                }
            }
        })
    }

    // Gets the details for our home page attractions based on our current coordinates
    fun fetchAttractionDetails(context: Context, locationId: String, listener: DetailFetchListener) {
        val url = "$locationId/details?language=en&currency=USD&key=$apiKey"
        val call = service.getAttractionDetails(baseUrl + url)

        call.enqueue(object : retrofit2.Callback<AttractionDetailsResponse> {
            override fun onResponse(call: Call<AttractionDetailsResponse>, response: retrofit2.Response<AttractionDetailsResponse>) {
                if (response.isSuccessful) {
                    val details = response.body()
                    if (details != null) {
                        Log.d(
                            "TripAdvisorManager",
                            "Details fetched successfully: ${details.description}"
                        )
                        listener.onDetailsFetched(details)
                    } else {
                        Log.d("TripAdvisorManager", "No details found in the response.")
                        listener.onDetailsFetchFailed("No details available")
                    }
                } else {
                    Log.e(
                        "TripAdvisorManager",
                        "Failed to fetch details: ${response.errorBody()?.string()}"
                    )
                    listener.onDetailsFetchFailed("Failed to fetch details: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<AttractionDetailsResponse>, t: Throwable) {
                Log.e("TripAdvisorManager", "Network error while fetching details: ${t.message}")
                listener.onDetailsFetchFailed("Network error: ${t.message}")
            }
        })
    }



    fun String.urlEncode(): String = java.net.URLEncoder.encode(this, "UTF-8")


    interface TripAdvisorService {
        @GET
        fun searchLocations(@Url url: String): Call<LocationSearchResponse>

        @GET
        fun getLocationPhotos(@Url url: String): Call<PhotoResponse>

        @GET
        fun getAttractionDetails(@Url url: String): Call<AttractionDetailsResponse>

  }

    // data classes necessary for our responses
    data class AttractionDetail(
        val locationID: String,
        val name: String,
        var imageUrl: String? = null
    )
    data class AttractionDetailsResponse(
        val location_id: String,
        val name: String,
        val description: String,
        val web_url: String,
        val address_obj: Address,
        val ancestors: List<Ancestor>,
        val latitude: String,
        val longitude: String,
        val timezone: String,
        val email: String?,
        val phone: String,
        val website: String,
        val write_review: String,
        val ranking_data: RankingData,
        val rating: String,
        val rating_image_url: String,
        val num_reviews: String,
        val photo_count: String,
        val see_all_photos: String,
        val category: Category,
        val subcategory: List<SubCategory>,
        val groups: List<Group>,
        val trip_types: List<TripType>,
        val awards: List<Any>
    )

    data class Address(
        val street1: String,
        val street2: String?,
        val city: String,
        val state: String,
        val country: String,
        val postalcode: String,
        val address_string: String
    )

    data class Ancestor(
        val level: String,
        val name: String,
        val location_id: String
    )

    data class RankingData(
        val geo_location_id: String,
        val ranking_string: String,
        val geo_location_name: String,
        val ranking_out_of: String,
        val ranking: String
    )

    data class Category(
        val name: String,
        val localized_name: String
    )

    data class SubCategory(
        val name: String,
        val localized_name: String
    )

    data class Group(
        val name: String,
        val localized_name: String,
        val categories: List<Category>
    )

    data class TripType(
        val name: String,
        val localized_name: String,
        val value: String
    )

    data class LocationSearchResponse(val data: List<LocationData>)
    data class LocationData(val location_id: String, val name: String)
    data class PhotoResponse(val data: List<PhotoData>)
    data class PhotoData(val images: ImageDetail)
    data class ImageDetail(val original: Original)

    data class Original(val url: String)
}
typealias ImageFetchCallback = (imageUrl: String) -> Unit