package com.example.tripplanner


import android.content.Context
//import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url

class TripAdvisorManager(
    private val context: Context,
    private val cityName: String,
    private val listener: AttractionFetchListener? = null,
    private val imageListener: ImageFetchListener? = null
) {

    interface AttractionFetchListener {
        fun onAttractionsFetched(attractions: List<String>)
        fun onAttractionFetchFailed(errorMessage: String)
    }

    interface ImageFetchListener {
        fun onImageFetched(imageUrl: String)
        fun onImageFetchFailed(errorMessage: String)
    }

    private val apiKey = "TRIP_ADVISOR_KEY"
    private val baseUrl = "https://api.content.tripadvisor.com/api/v1/location/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create(TripAdvisorService::class.java)

    fun fetchData() {
        fetchLocationId { locationId ->
            fetchImage(locationId)
            fetchAttractions()

        }
    }


    private fun fetchLocationId(callback: (String) -> Unit) {
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

    private fun fetchImage(locationId: String) {
        val url = "$locationId/photos?language=en&key=$apiKey"
        val call = service.getLocationPhotos(url)
        call.enqueue(object : retrofit2.Callback<PhotoResponse> {
            override fun onResponse(call: Call<PhotoResponse>, response: retrofit2.Response<PhotoResponse>) {
                val imageUrl = response.body()?.data?.firstOrNull()?.images?.original?.url ?: ""
                imageListener?.onImageFetched(imageUrl)
                //Picasso.get().load(imageUrl).into(imageView)
            }

            override fun onFailure(call: Call<PhotoResponse>, t: Throwable) {
                imageListener?.onImageFetchFailed("Network error: ${t.message}")
            }
        })
    }

    private fun fetchAttractions() {
        val url = "search?searchQuery=$cityName&category=attractions&language=en&key=$apiKey"
        val call = service.searchLocations(url)
        call.enqueue(object : retrofit2.Callback<LocationSearchResponse> {
            override fun onResponse(call: Call<LocationSearchResponse>, response: retrofit2.Response<LocationSearchResponse>) {
                val attractions = response.body()?.data?.map { it.name } ?: listOf()
                //updateAttractionsUI(attractions)
                listener?.onAttractionsFetched(attractions)
            }

            override fun onFailure(call: Call<LocationSearchResponse>, t: Throwable) {
                // Handle error, could log or display to user
            }
        })
    }

/*
    private fun updateAttractionsUI(attractions: List<String>) {
        val adapter = AttractionsAdapter(attractions)
        attractionsRecyclerView.adapter = adapter
    }

    class AttractionsAdapter(private val attractions: List<String>) :
        RecyclerView.Adapter<AttractionsAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.attraction_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.attractionName.text = attractions[position]
        }

        override fun getItemCount() = attractions.size

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val attractionName: TextView = view.findViewById(R.id.attractionName)
        }
    }
*/
    interface TripAdvisorService {
        @GET
        fun searchLocations(@Url url: String): Call<LocationSearchResponse>

        @GET
        fun getLocationPhotos(@Url url: String): Call<PhotoResponse>
    }

    data class LocationSearchResponse(val data: List<LocationData>)
    data class LocationData(val location_id: String, val name: String)
    data class PhotoResponse(val data: List<PhotoData>)
    data class PhotoData(val images: ImageDetail)
    data class ImageDetail(val original: Original)

    data class Original(val url: String)
}
