package com.example.tripplanner.apis.tripadvisor

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url
import java.net.URLEncoder
import java.util.concurrent.CountDownLatch

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

    private val apiKey = "8F662D5533D74F3AAAF65FBC98BE1703"
    private val baseUrl = "https://api.content.tripadvisor.com/api/v1/location/"

    // retrofit instance for location search endpoint
    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create(TripAdvisorService::class.java)

    fun fetchData(context: Context, searchQuery: String, latLong: String?, category: String?, listener: AttractionFetchListener) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Call the internal fetchAttractions to get basic attraction data
                val attractions = fetchAttractions(context, searchQuery, latLong, category)

                // Concurrently fetch images for each attraction
                val updatedAttractions = attractions.map { attraction ->
                    async {
                        val imageUrl = fetchImage(attraction.locationID)
                        attraction.copy(imageUrl = imageUrl)  // Update each attraction with its image URL
                    }
                }.awaitAll()

                // Notify the listener on the main thread
                withContext(Dispatchers.Main) {
                    listener.onAttractionsFetched(updatedAttractions)
                }
            } catch (e: Exception) {
                // Move error handling also to the main thread to handle UI updates
                withContext(Dispatchers.Main) {
                    listener.onAttractionFetchFailed(e.message ?: "Failed to fetch attractions")
                }
            }
        }
    }

    suspend fun fetchImage(locationId: String): String = withContext(Dispatchers.IO) {
        try {
            val url = "$locationId/photos?language=en&key=$apiKey"
            val response = service.getLocationPhotos(url).execute()
            if (response.isSuccessful) {
                response.body()?.data?.firstOrNull()?.images?.original?.url.orEmpty()
            } else {
                Log.e("TripAdvisorManager", "Error fetching image for location ID $locationId: ${response.errorBody()?.string()}")
                ""
            }
        } catch (e: Exception) {
            Log.e("TripAdvisorManager", "Failed to fetch image for location ID $locationId: ${e.message}")
            ""
        }
    }


    suspend fun fetchAttractions(context: Context, searchQuery: String, latLong: String?, category: String?): List<AttractionDetail> = withContext(Dispatchers.IO) {
        try {
            val url = buildString {
                append("https://api.content.tripadvisor.com/api/v1/location/search?")
                append("searchQuery=${URLEncoder.encode(searchQuery, "UTF-8")}")
                if (!latLong.isNullOrEmpty()) append("&latLong=${URLEncoder.encode(latLong, "UTF-8")}")
                if (!category.isNullOrEmpty()) append("&category=${URLEncoder.encode(category, "UTF-8")}")
                append("&language=en&key=$apiKey")
            }
            val response = service.searchLocations(url).execute()
            if (response.isSuccessful) {
                response.body()?.data?.map { locationData ->
                    AttractionDetail(locationID = locationData.location_id, name = locationData.name, imageUrl = null)
                } ?: emptyList()
            } else {
                throw Exception("Error fetching data: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("TripAdvisorManager", "Network error: ${e.message}")
            emptyList()
        }
    }



    suspend fun fetchCityImage(cityName: String): String = coroutineScope {
        val locationId = fetchLocationId(cityName)
        if (locationId.isNotEmpty()) {
            fetchImage(locationId)
        } else {
            ""
        }
    }

    suspend fun fetchLocationId(cityName: String): String = withContext(Dispatchers.IO) {
        try {
            val url = "search?searchQuery=$cityName&language=en&key=$apiKey"
            val response = service.searchLocations(url).execute()
            if (response.isSuccessful) {
                response.body()?.data?.firstOrNull()?.location_id ?: ""
            } else {
                Log.e("API", "Failed to fetch location ID: ${response.errorBody()?.string()}")
                ""
            }
        } catch (e: Exception) {
            Log.e("API", "Exception fetching location ID: ${e.message}")
            ""
        }
    }

    suspend fun fetchSearchTheLocation(
        cityName: String,
        category: String,
        latLong: String,
        searchQuery: String
    ): List<AttractionDetail> = withContext(Dispatchers.IO) {
        val attractions = mutableMapOf<String, AttractionDetail>()
        try {
            // Build the search URL
            val url = "search?key=$apiKey&searchQuery=${searchQuery.urlEncode()}&category=$category&latLong=$latLong&language=en"
            val response = service.searchLocations(baseUrl + url).execute()

            if (response.isSuccessful && response.body() != null) {
                val locationDataList = response.body()!!.data

                // Fetch images concurrently for each location
                val deferredAttractions = locationDataList.map { locationData ->
                    async {
                        val attractionDetail = AttractionDetail(
                            locationID = locationData.location_id,
                            name = locationData.name,
                            imageUrl = null
                        )

                        // Fetch the image URL
                        val imageUrl = fetchImageSynchronously(locationData.location_id)
                        attractionDetail.copy(imageUrl = imageUrl)
                    }
                }

                // Await all async tasks to complete
                deferredAttractions.awaitAll().forEach { updatedAttraction ->
                    attractions[updatedAttraction.locationID] = updatedAttraction
                }
            } else {
                Log.e("TripAdvisorManager", "Error fetching data: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("TripAdvisorManager", "Network error: ${e.message}")
        }

        return@withContext attractions.values.toList()
    }

    //This helper function fetches images synchronously.
    private suspend fun fetchImageSynchronously(locationId: String): String = withContext(Dispatchers.IO) {
        try {
            val url = "$locationId/photos?language=en&key=$apiKey"
            val response = service.getLocationPhotos(url).execute()
            response.body()?.data?.firstOrNull()?.images?.original?.url.orEmpty()
        } catch (e: Exception) {
            Log.e("TripAdvisorManager", "Failed to fetch image for location ID $locationId: ${e.message}")
            ""
        }
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