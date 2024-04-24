package com.example.tripplanner

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Itinerary::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itineraryDao(): ItineraryDao
}