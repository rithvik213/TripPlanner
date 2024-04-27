package com.example.tripplanner

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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


