package com.example.tripplanner

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "itineraries")
data class Itinerary(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "trip_dates") val tripDates: String,
    @ColumnInfo(name = "city_name") val cityName: String,
    @ColumnInfo(name = "itinerary_details") val itineraryDetails: String,
    @ColumnInfo(name = "image_url") val imageUrl: String

)