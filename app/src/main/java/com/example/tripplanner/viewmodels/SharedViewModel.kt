package com.example.tripplanner.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

// Allows us to track what the user selected as their preferred attraction types form the trip_search fragment
// to be used for generating an itinerary
class SharedViewModel : ViewModel() {
    val selectedAttractions = MutableLiveData<List<String>>()
    val originAirport: MutableLiveData<String> = MutableLiveData()
    val destinationAirport: MutableLiveData<String> = MutableLiveData()
    val budget: MutableLiveData<String> = MutableLiveData()
    val cityName: MutableLiveData<String> = MutableLiveData()
    val budgetProgress: MutableLiveData<Int> = MutableLiveData()
    val departDateDisplay = MutableLiveData<String>()
    val returnDateDisplay = MutableLiveData<String>()
    val departDateISO = MutableLiveData<String>()
    val returnDateISO = MutableLiveData<String>()

    fun setDepartDate(year: Int, month: Int, day: Int) {
        val displayFormat = String.format("%02d/%02d/%d", day, month + 1, year)
        val isoFormat = String.format("%d-%02d-%02d", year, month + 1, day)
        departDateDisplay.value = displayFormat
        departDateISO.value = isoFormat
    }

    fun setReturnDate(year: Int, month: Int, day: Int) {
        val displayFormat = String.format("%02d/%02d/%d", day, month + 1, year)
        val isoFormat = String.format("%d-%02d-%02d", year, month + 1, day)
        returnDateDisplay.value = displayFormat
        returnDateISO.value = isoFormat
    }

    fun resetViewModel() {
        selectedAttractions.value = emptyList()
        originAirport.value = ""
        destinationAirport.value = ""
        budget.value = "0"
        cityName.value = ""
        budgetProgress.value = 0
        departDateDisplay.value = "Depart"
        returnDateDisplay.value = "Return"
        departDateISO.value = ""
        returnDateISO.value = ""
    }
}

