package com.example.tripplanner.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import com.google.api.services.calendar.CalendarScopes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// A ViewModel to keep track of the itinerary for our trip_page specifically in order to parse
// our database value for the itinerary details
class ParsedItineraryViewModel : ViewModel() {

    private val _itinerary = MutableLiveData<String>()
    val itinerary: LiveData<String>
        get() = _itinerary

    fun setItinerary(itinerary: String) {
        _itinerary.value = itinerary
    }

    fun parseItinerary(itinerary: String): Map<String, List<EventDetail>> {
        val parsed = mutableMapOf<String, MutableList<EventDetail>>()
        val regex = """\(Day \d+: ([^|]+)\|([^|]+)\|?([^)]*)\)""".toRegex()

        for (match in regex.findAll(itinerary)) {
            val date = match.groupValues[1].trim()
            val name = match.groupValues[2].trim()
            val time = match.groupValues[3].trim()

            val eventDetail = EventDetail(name, time)

            if (parsed[date] == null) {
                parsed[date] = mutableListOf(eventDetail)
            } else {
                parsed[date]!!.add(eventDetail)
            }
        }
        return parsed
    }


    fun addEventsToGoogleCalendar(
        context: Context,
        account: GoogleSignInAccount,
        eventsMap: Map<String, List<EventDetail>>
    ) {
        Log.d("GoogleCalendar", "Parsed Itinerary: $eventsMap")
        CoroutineScope(Dispatchers.IO).launch {
            val transport = GoogleNetHttpTransport.newTrustedTransport()
            val jsonFactory: JsonFactory = GsonFactory.getDefaultInstance()
            val credential = GoogleAccountCredential.usingOAuth2(
                context, listOf(CalendarScopes.CALENDAR)
            ).apply {
                selectedAccount = account.account
            }

            val calendarService = Calendar.Builder(transport, jsonFactory, credential)
                .setApplicationName("TripPlanner")
                .build()

            for ((date, events) in eventsMap) {
                for (eventDetail in events) {
                    try {
                        val sdf = SimpleDateFormat("MMMM dd, yyyy hh:mma", Locale.ENGLISH)
                        val startDateTime = sdf.parse("$date ${eventDetail.time}")?.time
                        if (startDateTime == null) {
                            Log.e("GoogleCalendar", "Failed to parse start time for event: ${eventDetail.name}")
                            continue
                        }
                        val endDateTime = startDateTime + 3600000  // Adds one hour in milliseconds

                        val event = Event().apply {
                            summary = eventDetail.name
                            description = "Scheduled time for event: ${eventDetail.time}"
                            start = EventDateTime().setDateTime(DateTime(startDateTime))
                            end = EventDateTime().setDateTime(DateTime(endDateTime))
                        }

                        calendarService.events().insert("primary", event).execute()
                        Log.d("GoogleCalendar", "Event added to Google Calendar for $date at ${eventDetail.time}")

                    } catch (e: Exception) {
                        Log.e("GoogleCalendar", "Error adding event to Google Calendar", e)
                    }
                }
            }
        }
    }


    data class EventDetail(val name: String, val time: String)
}
