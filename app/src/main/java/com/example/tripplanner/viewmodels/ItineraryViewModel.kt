package com.example.tripplanner.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.tripplanner.data.Itinerary
import com.example.tripplanner.database.ItineraryRepository
import com.example.tripplanner.database.MyApp

// Allows us to get itineraries that have been inserted to the database already
class ItineraryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ItineraryRepository
    val allItineraries: LiveData<List<Itinerary>>

    init {
        val itineraryDao = (application as MyApp).database.itineraryDao()
        repository = ItineraryRepository(itineraryDao)
        allItineraries = repository.allItineraries
    }

    fun getItineraryById(itineraryId: Int): LiveData<Itinerary> {
        return repository.getItineraryById(itineraryId)
    }
}


