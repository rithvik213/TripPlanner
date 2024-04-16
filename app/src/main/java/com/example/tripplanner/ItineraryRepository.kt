package com.example.tripplanner

import androidx.lifecycle.LiveData

class ItineraryRepository(private val itineraryDao: ItineraryDao) {
    val allItineraries: LiveData<List<Itinerary>> = itineraryDao.getAllItinerariesLiveData()

    suspend fun insert(itinerary: Itinerary) {
        itineraryDao.insertItinerary(itinerary)
    }
}
