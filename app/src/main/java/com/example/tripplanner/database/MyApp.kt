package com.example.tripplanner.database

import android.app.Application
import androidx.room.Room

class MyApp : Application() {
    lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "trip-planner-database"
        ).fallbackToDestructiveMigration().build()
    }
}