package com.example.tripplanner.apis.amadeus.data

import com.google.gson.annotations.SerializedName

data class HotelResponse(
    @SerializedName("data")
    val hotels: List<Hotel>
)

data class Hotel(
    @SerializedName("chainCode")
    val chainCode: String,
    @SerializedName("iataCode")
    val iataCode: String,
    @SerializedName("dupeId")
    val dupeId: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("hotelId")
    val hotelId: String,
    @SerializedName("geoCode")
    val geoCode: GeoCode,
    @SerializedName("address")
    val address: Address,
    @SerializedName("lastUpdate")
    val lastUpdate: String
)

data class GeoCode(
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double
)

data class Address(
    @SerializedName("countryCode")
    val countryCode: String
)
