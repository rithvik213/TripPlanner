package com.example.tripplanner.data

// Data classes that are used to make our responses to serpAPI and TripAdvisor both the same type
data class Excursion(
    val name: String,
    val time: String,
    var imageUrl: String = ""
)

data class DayItinerary(
    val date: String,
    val excursions: List<Excursion>
)

