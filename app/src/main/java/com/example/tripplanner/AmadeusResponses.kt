package com.example.flightapitest

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.http.Header
import retrofit2.http.Query
import java.util.Random

data class ToAnywhereInfo(
    val origin: String?,
    val destination: String?,
    val departureDate: String?,
    val returnDate: String?,
    val totalPrice: String?
)

//RETURNS RANDOM RESULT FROM CACHED CHEAPEST FLIGHTS FROM ORIGIN
suspend fun fetchToAnywhere(
    origin: String,         //IATA code (LON)
    departureDate: String,  //YYYY-MM-DD (2024-05-04)
    duration: Int,          //Int, (6)
    maxPrice: Int           //max price per traveller (200)
): ToAnywhereInfo? {
    return withContext(Dispatchers.IO) {
        try {
            val response = AmadeusApiClient.client.toAnywhere(
                authorization = SimpleTokenManager.getToken()!!,
                origin = origin,
                departureDate = departureDate,
                duration = duration,
                maxPrice = maxPrice
            )

            if (response.data.isNotEmpty()) {
                val randomFlight = response.data[Random().nextInt(response.data.size)]
//                Log.i("origin",randomFlight.origin)
//                Log.i("dest",randomFlight.destination)
//                Log.i("depart",randomFlight.departureDate)
//                Log.i("return",randomFlight.returnDate)
//                Log.i("price",randomFlight.price.total)
                ToAnywhereInfo(
                    origin = randomFlight.origin,
                    destination = randomFlight.destination,
                    departureDate = randomFlight.departureDate,
                    returnDate = randomFlight.returnDate,
                    totalPrice = randomFlight.price.total
                )
            } else {
                Log.e("ToAnywhereResponse", "Exception: Response Empty")
                null
            }
        } catch (e: Exception) {
                Log.e("ToAnywhereFailure", "Exception: ${e.message}")
                null
        }
    }
}

//RETURNS REALTIME FLIGHT DATA WITH GRANULAR INFORMATION, SEE FlightOffersResponse.kt
suspend fun fetchFlightOffers(
    originLoc: String,      //IATA code (LON)
    destLoc: String,        //IATA code (MAD)
    departureDate: String,  //YYYY-MM-DD (2024-05-04)
    returnDate: String,     //YYYY-MM-DD (2024-05-04)
    adults: Int,            //Int (1)
    maxPrice: Int,          //maxPrice per traveller, Int (200)
    currencyCode: String,   //currencyCode (USD)
    max: Int                //max results (1)
): FlightOffersResponse? {
    return withContext(Dispatchers.IO) {
        try {
            val response = AmadeusApiClient.client.flightOffers(
                authorization = SimpleTokenManager.getToken()!!,
                originLocationCode = originLoc,
                destinationLocationCode = destLoc,
                departureDate =  departureDate,
                returnDate = returnDate,
                adults = adults,
                maxPrice = maxPrice,
                currencyCode = currencyCode,
                max = max
            )

            if (response.flightOffers.isNotEmpty()) {
                Log.i("FlightOffersSuccess", "FlightOffers Succeeded")
                response
            } else {
                Log.e("FlightOffersResponse", "Exception: Response Empty")
                null
            }
        } catch (e: Exception) {
            Log.e("FlightOffersFailure", "Exception: ${e.message}")
            null
        }
    }
}

//RETURNS HOTELS BY CITY IATA CODE, DOES NOT CONTAIN GRANULAR DATA
suspend fun fetchHotelsByCity(
    cityCode: String,   //IATA code (LON)
    radius: Int,        //radius in KM or MI (5)
    radiusUnit: String  //units (KM or MI)
): HotelResponse? {
    return withContext(Dispatchers.IO) {
        try {
            val response = AmadeusApiClient.client.hotelsByCity(
                authorization = SimpleTokenManager.getToken()!!,
                cityCode = cityCode,
                radius = radius,
                radiusUnit = radiusUnit //Could possibly just hardcode this to MI
            )
            if (response.hotels.isNotEmpty()) {
                Log.i("HotelsSuccess", "Hotels Succeeded")
                response
            } else {
                Log.e("HotelsResponse", "Exception: Response Empty")
                null
            }
        } catch (e: Exception) {
            Log.e("HotelsFailure", "Exception: ${e.message}")
            null
        }
    }
}

//RETURNS RECOMMENDED LOCATIONS BASED ON PREVIOUSLY LIKED LOCATIONS
suspend fun fetchRecommendedLocations(
    cityCodes: String,          //IATA code (LON)
    travelerCountryCode: String //origin of traveller (US)
): RecommendedLocationsResponse? {
    return withContext(Dispatchers.IO) {
        try {
            val response = AmadeusApiClient.client.recommendedLocations(
                authorization = SimpleTokenManager.getToken()!!,
                cityCodes = cityCodes,
                travelerCountryCode = travelerCountryCode,
            )
            if (response.recommendedLocations.isNotEmpty()) {
                Log.i("RecommendedLocationsSuccess", "RecommendedLocations Succeeded")
                response
            } else {
                Log.e("RecommendLocationsResponse", "Exception: Response Empty")
                null
            }
        } catch (e: Exception) {
            Log.e("RecommendedLocationsFailure", "Exception: ${e.message}")
            null
        }
    }
}




