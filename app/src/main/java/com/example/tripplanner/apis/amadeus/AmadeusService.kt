package com.example.tripplanner.apis.amadeus

import com.example.tripplanner.apis.amadeus.data.FlightOffersResponse
import com.example.tripplanner.apis.amadeus.data.HotelResponse
import com.example.tripplanner.apis.amadeus.data.RecommendedLocationsResponse
import com.example.tripplanner.apis.amadeus.data.ToAnywhereResponse
import com.example.tripplanner.apis.amadeus.data.TokenResponse
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query


interface AmadeusApiService {

    @GET("/v1/shopping/flight-destinations")
    suspend fun toAnywhere(
        @Header("Authorization") authorization: String,
        @Query("departureDate") departureDate: String,
        @Query("origin") origin: String,
        @Query("duration") duration: Int,
        @Query("maxPrice") maxPrice: Int
    ): ToAnywhereResponse

    @GET("/v2/shopping/flight-offers")
    suspend fun flightOffers(
        @Header("Authorization") authorization: String,
        @Query("originLocationCode") originLocationCode: String,
        @Query("destinationLocationCode") destinationLocationCode: String,
        @Query("departureDate") departureDate: String,
        @Query("returnDate") returnDate: String,
        @Query("nonStop") nonStop: Boolean = true,
        @Query("adults") adults: Int,
        @Query("maxPrice") maxPrice: Int,
        @Query("currencyCode") currencyCode: String,
        @Query("max") max: Int
    ): FlightOffersResponse

    @GET("/v1/reference-data/locations/hotels/by-city")
    suspend fun hotelsByCity(
        @Header("Authorization") authorization: String,
        @Query("cityCode") cityCode: String,
        @Query("radius") radius: Int,
        @Query("radiusUnit") radiusUnit: String
    ): HotelResponse

    @GET("/v1/reference-data/recommended-locations")
    suspend fun recommendedLocations(
        @Header("Authorization") authorization: String,
        @Query("cityCodes") cityCodes: String,
        @Query("travelerCountryCode") travelerCountryCode: String
    ): RecommendedLocationsResponse

    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("/v1/security/oauth2/token")
    fun getToken(
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("grant_type") grantType: String
    ): Call<TokenResponse>


}