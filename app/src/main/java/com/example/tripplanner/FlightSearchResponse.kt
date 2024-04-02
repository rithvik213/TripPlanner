package com.example.tripplanner

data class FlightSearchResponse(
    val data: Data,
    val status: Boolean,
    val message: String
)

data class Data(
    val everywhereDestination: EverywhereDestination
)

data class EverywhereDestination(
    val results: List<Result>
)

data class Result(
    val id: String,
    val type: String,
    val content: Content
)

data class Content(
    val location: Location,
    val flightQuotes: FlightQuotes?,
    val image: Image? // Added to handle the image field
)

data class Location(
    val name: String,  // Country name
    val state: String?,  // State or region name
    val airportName: String?,  // Airport name
    val airportCode: String?  // Airport code
)


data class FlightQuotes(
    val cheapest: PriceQuote?,
    val direct: PriceQuote? // Added to handle direct flights, which are optional and not present in all quotes
)

data class PriceQuote(
    val price: String,
    val rawPrice: Double,
    val direct: Boolean?
)

data class Image(
    val url: String
)
