package com.example.tripplanner.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.tripplanner.data.Itinerary

@Dao
interface ItineraryDao {
    @Insert
    suspend fun insertItinerary(itinerary: Itinerary)

    @Query("SELECT * FROM itineraries WHERE user_id = :userId")
    suspend fun getItinerariesByUser(userId: String): List<Itinerary>

    @Query("SELECT * FROM itineraries")
    fun getAllItineraries(): List<Itinerary>

    @Query("SELECT * FROM itineraries")
    fun getAllItinerariesLiveData(): LiveData<List<Itinerary>>

    @Query("SELECT * FROM itineraries WHERE id = :itineraryId")
    fun getItineraryById(itineraryId: Int): LiveData<Itinerary>

    @Query("SELECT lat_long FROM itineraries WHERE user_id = :userId")
    fun getUserLatLongs(userId: String): List<String>
}