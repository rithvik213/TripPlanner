package com.example.tripplanner.data


data class Excursion(
    val name: String,
    val time: String,
    var imageUrl: String = ""
)

data class DayItinerary(
    val date: String,
    val excursions: List<Excursion>
)

