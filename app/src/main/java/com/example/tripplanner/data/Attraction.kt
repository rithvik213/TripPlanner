package com.example.tripplanner.data

data class Attraction(
    val id: String, //This is the TripAdvisor locationID
    val title: String,
    val imageUrl: String? = null
)