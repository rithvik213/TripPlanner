package com.example.tripplanner.apis.amadeus.data

import com.google.gson.annotations.SerializedName

data class FlightOffersResponse(
    @SerializedName("data")
    val flightOffers: List<FlightOffer>
)

data class FlightOffer(
    @SerializedName("itineraries")
    val itineraries: List<Itinerary>,
    @SerializedName("price")
    val price: OfferPrice,
    @SerializedName("validatingAirlineCodes")
    val validatingAirlineCodes: List<String>
)

data class Itinerary(
    @SerializedName("segments")
    val segments: List<Segment>
)

data class Segment(
    @SerializedName("departure")
    val departure: AirportInfo,
    @SerializedName("arrival")
    val arrival: AirportInfo,
    @SerializedName("carrierCode")
    val carrierCode: String,
    @SerializedName("number")
    val number: String,
    @SerializedName("duration")
    val duration: String
)

data class AirportInfo(
    @SerializedName("iataCode")
    val iataCode: String,
    @SerializedName("terminal")
    val terminal: String,
    @SerializedName("at")
    val dateTime: String
)

data class OfferPrice(
    @SerializedName("currency")
    val currency: String,
    @SerializedName("total")
    val total: String
)
