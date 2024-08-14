package com.example.tripplanner.database

import android.app.Application
import androidx.room.Room
import com.example.tripplanner.apis.ApiKeyProvider

class MyApp : Application() {
    lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "trip-planner-database"
        ).fallbackToDestructiveMigration().build()

        //Initialize ApiKeyProvider with the application context
        ApiKeyProvider.initialize(this)
    }

    companion object {
        lateinit var instance: MyApp
            private set
    }
}