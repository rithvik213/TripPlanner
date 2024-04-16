package com.example.tripplanner

import java.io.Serializable

data class Excursion(
    val name: String,
    val time: String
)

data class DayItinerary(
    val date: String,
    val excursions: List<Excursion>
)

