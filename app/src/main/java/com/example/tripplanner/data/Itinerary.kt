package com.example.tripplanner.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

// A data class to specify all the relevant information for a trip/itinerary
// to be used in our results and trip_page fragments
@Entity(tableName = "itineraries")
data class Itinerary(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "trip_dates") val tripDates: String,
    @ColumnInfo(name = "city_name") val cityName: String,
    @ColumnInfo(name = "itinerary_details") val itineraryDetails: String,
    @ColumnInfo(name = "image_url") val imageUrl: String,
    //Additional fields for flight information
    @ColumnInfo(name = "departure_airport") val departureAirport: String,
    @ColumnInfo(name = "arrival_airport") val arrivalAirport: String,
    @ColumnInfo(name = "departure_date") val departureDate: String,
    @ColumnInfo(name = "return_date") val returnDate: String,
    @ColumnInfo(name = "departure_time") val departureTime: String,
    @ColumnInfo(name = "arrival_time") val arrivalTime: String,
    @ColumnInfo(name = "departure_airport2") val departureAirport2: String?,
    @ColumnInfo(name = "arrival_airport2") val arrivalAirport2: String?,
    @ColumnInfo(name = "departure_time2") val departureTime2: String?,
    @ColumnInfo(name = "arrival_time2") val arrivalTime2: String?,
    @ColumnInfo(name = "departure_terminal") val departureTerminal: String,
    @ColumnInfo(name = "arrival_terminal") val arrivalTerminal: String,
    @ColumnInfo(name = "departure_terminal2") val departureTerminal2: String?,
    @ColumnInfo(name = "arrival_terminal2") val arrivalTerminal2: String?,
    @ColumnInfo(name = "total_price") val totalPrice: String
)
