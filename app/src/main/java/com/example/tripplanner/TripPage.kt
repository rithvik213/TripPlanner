package com.example.tripplanner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TripPage : Fragment() {
    private lateinit var imageView: ImageView
    private lateinit var departDateTextView: TextView
    private lateinit var returnDateTextView: TextView
    private lateinit var departureAirportTextView: TextView
    private lateinit var departureAirport2TextView: TextView
    private lateinit var arrivalAirportTextView: TextView
    private lateinit var arrivalAirport2TextView: TextView
    private lateinit var departTimeTextView: TextView
    private lateinit var departTime2TextView: TextView
    private lateinit var arrivalTimeTextView: TextView
    private lateinit var arrivalTime2TextView: TextView
    private lateinit var viewModel: ItineraryViewModel
    //private lateinit var itineraryViewPager: ViewPager2
    //private lateinit var excursionAdapter: ExcursionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_trip_page, container, false)

        imageView = view.findViewById(R.id.trippagepicture)
        departDateTextView = view.findViewById(R.id.departDate)
        returnDateTextView = view.findViewById(R.id.returnDate)
        departureAirportTextView = view.findViewById(R.id.departureAirport)
        departureAirport2TextView = view.findViewById(R.id.departureAirport2)
        arrivalAirportTextView = view.findViewById(R.id.arrivalAirport)
        arrivalAirport2TextView = view.findViewById(R.id.arrivalAirport2)
        departTimeTextView = view.findViewById(R.id.departTime)
        departTime2TextView = view.findViewById(R.id.departTime2)
        arrivalTimeTextView = view.findViewById(R.id.arrivalTime)
        arrivalTime2TextView = view.findViewById(R.id.arrivalTime2)

        val tripId = arguments?.getInt("tripId") ?: -1
        setupViewModel(tripId)

        return view
    }

    private fun setupViewModel(tripId: Int) {
        viewModel = ViewModelProvider(this).get(ItineraryViewModel::class.java)
        if (tripId != -1) {
            viewModel.getItineraryById(tripId).observe(viewLifecycleOwner) { itinerary ->
                Glide.with(this).load(itinerary.imageUrl).into(imageView)
                departDateTextView.text = itinerary.departureDate
                returnDateTextView.text = itinerary.returnDate
                departureAirportTextView.text = itinerary.departureAirport
                departureAirport2TextView.text = itinerary.departureAirport2
                arrivalAirportTextView.text = itinerary.arrivalAirport
                arrivalAirport2TextView.text = itinerary.arrivalAirport2
                departTimeTextView.text = itinerary.departureTime
                departTime2TextView.text = itinerary.departureTime2
                arrivalTimeTextView.text = itinerary.arrivalTime
                arrivalTime2TextView.text = itinerary.arrivalTime2

                //val daysItinerary = parseItineraryDetails(itinerary.itineraryDetails)
                //val itineraryPagerAdapter = ItineraryPagerAdapter(daysItinerary, this)
                //itineraryViewPager.adapter = itineraryPagerAdapter

                //excursionAdapter.updateExcursions(itinerary.excursions)


            }
        } else {

        }
    }

    private fun parseItineraryDetails(details: String): List<DayItinerary> {
        return try {
            val type = object : TypeToken<List<DayItinerary>>() {}.type
            Gson().fromJson(details, type)
        } catch (e: Exception) {
            listOf<DayItinerary>()
        }
    }
}
