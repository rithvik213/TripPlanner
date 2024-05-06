package com.example.tripplanner.data

// Data Class for our actual attraction information needed for our home page
data class Attraction(
    val id: String, //This is the TripAdvisor locationID
    val title: String,
    val imageUrl: String? = null
)