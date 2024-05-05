package com.example.tripplanner.database

import androidx.lifecycle.LiveData
import com.example.tripplanner.Itinerary

class ItineraryRepository(private val itineraryDao: ItineraryDao) {
    val allItineraries: LiveData<List<Itinerary>> = itineraryDao.getAllItinerariesLiveData()

    suspend fun insert(itinerary: Itinerary) {
        itineraryDao.insertItinerary(itinerary)
    }

    fun getItineraryById(itineraryId: Int): LiveData<Itinerary> {
        return itineraryDao.getItineraryById(itineraryId)
    }
}
