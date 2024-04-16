package com.example.flightapitest

import com.google.gson.annotations.SerializedName

data class ToAnywhereResponse(
    @SerializedName("data")
    val data: List<FlightDestination>
)

data class FlightDestination(
    @SerializedName("type")
    val type: String,

    @SerializedName("origin")
    val origin: String,

    @SerializedName("destination")
    val destination: String,

    @SerializedName("departureDate")
    val departureDate: String,

    @SerializedName("returnDate")
    val returnDate: String,

    @SerializedName("price")
    val price: ToAnywherePrice,

    @SerializedName("links")
    val links: Links
)

data class ToAnywherePrice(
    @SerializedName("total")
    val total: String
)

data class Links(
    @SerializedName("flightDates")
    val flightDates: String,

    @SerializedName("flightOffers")
    val flightOffers: String
)
