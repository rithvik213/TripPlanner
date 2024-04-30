package com.example.tripplanner.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.tripplanner.Itinerary

@Database(entities = [Itinerary::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itineraryDao(): ItineraryDao
}