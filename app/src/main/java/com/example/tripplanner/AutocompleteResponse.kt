package com.example.tripplanner

data class AutocompleteResponse(
    val data: List<DataItem>?,
    val status: Boolean,
    val message: String
)

data class DataItem(
    val presentation: Presentation,
    val navigation: Navigation
)

data class Presentation(
    val title: String,
    val suggestionTitle: String,
    val subtitle: String,
    val id: String
)

data class Navigation(
    val entityId: String,
    val entityType: String,
    val localizedName: String,
)


