package com.example.tripplanner.apis

import com.example.tripplanner.AutocompleteResponse
import com.example.tripplanner.apis.amadeus.data.FlightSearchResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface SkyscannerApiService {

    @GET("flights/search-everywhere")
    @Headers(
        "X-RapidAPI-Key: SKYSCANNER_KEY",
        "X-RapidAPI-Host: sky-scanner3.p.rapidapi.com"
    )
    fun searchFlightsEverywhere(
        @Query("fromEntityId") fromEntityId: String,
        @Query("year") year: String,
        @Query("month") month: String,
        @Query("locale") locale: String = "en-US",
        @Query("currency") currency: String = "USD",
        @Query("market") market: String = "US",
        @Query("adults") adults: Int = 1,
        @Query("children") children: Int = 0,
        @Query("infants") infants: Int = 0,
        @Query("cabinClass") cabinClass: String = "economy"
    ): Call<FlightSearchResponse>

    @GET("flights/auto-complete")
    @Headers(
        "X-RapidAPI-Key: SKYSCANNER_KEY",
        "X-RapidAPI-Host: sky-scanner3.p.rapidapi.com"
    )
    fun autocompleteLocation(
        @Query("query") query: String,
        @Query("locale") locale: String = "en-US"
    ): Call<AutocompleteResponse>



}
