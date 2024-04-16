package com.example.tripplanner


data class Excursion(
    val name: String,
    val time: String,
    val imageUrl: String = ""
)

data class DayItinerary(
    val date: String,
    val excursions: List<Excursion>
)

