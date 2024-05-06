package com.example.tripplanner.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

// Allows us to track what the user selected as their preferred attraction types form the trip_search fragment
// to be used for generating an itinerary
class SharedViewModel : ViewModel() {
    val selectedAttractions = MutableLiveData<List<String>>()
}