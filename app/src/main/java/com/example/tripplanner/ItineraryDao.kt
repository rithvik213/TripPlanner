package com.example.tripplanner

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ItineraryDao {
    @Insert
    suspend fun insertItinerary(itinerary: Itinerary)

    @Query("SELECT * FROM itineraries WHERE user_id = :userId")
    suspend fun getItinerariesByUser(userId: String): List<Itinerary>

    @Query("SELECT * FROM itineraries")
    fun getAllItineraries(): List<Itinerary>
}