package com.example.tripplanner.apis.amadeus.data

import com.google.gson.annotations.SerializedName

data class RecommendedLocationsResponse(
    @SerializedName("data")
    val recommendedLocations: List<RecommendedLocation>
)

data class RecommendedLocation(
    @SerializedName("subtype")
    val subtype: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("iataCode")
    val iataCode: String,
    @SerializedName("geoCode")
    val geoCode: RecommendedGeoCode,
    @SerializedName("type")
    val type: String,
    @SerializedName("relevance")
    val relevance: Double
)

data class RecommendedGeoCode(
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double
)
