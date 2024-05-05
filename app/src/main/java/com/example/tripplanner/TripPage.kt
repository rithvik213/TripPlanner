package com.example.tripplanner

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import java.text.SimpleDateFormat
import java.util.Locale
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.ParseException
import java.util.Calendar
import java.util.Date

class TripPage : Fragment() {
    private lateinit var imageView: ImageView
    private lateinit var departDateTextView: TextView
    private lateinit var location: TextView
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
    private lateinit var itineraryViewPager: ViewPager2
    private lateinit var excursionAdapter: ExcursionAdapter
    private lateinit var parsedItineraryViewModel: ParsedItineraryViewModel
    private lateinit var calendarButton: ImageButton
    private lateinit var itineraryToParse: String
    private lateinit var leftArrowButton: ImageButton
    private lateinit var rightArrowButton: ImageButton
    private lateinit var dayLabelTextView: TextView
    private lateinit var backButton: ImageButton

    private lateinit var daysItinerary: List<DayItinerary>

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
        location = view.findViewById(R.id.location)
        calendarButton = view.findViewById(R.id.calendar)
        parsedItineraryViewModel = ViewModelProvider(this).get(ParsedItineraryViewModel::class.java)
        itineraryViewPager = view.findViewById(R.id.itineraryViewPager)
        leftArrowButton = view.findViewById(R.id.leftArrow)
        rightArrowButton = view.findViewById(R.id.rightArrow)
        dayLabelTextView = view.findViewById(R.id.dayLabel)

        val tripId = arguments?.getInt("tripId") ?: -1
        setupViewModel(tripId)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val leftArrow = view.findViewById<ImageButton>(R.id.leftArrow)
        val rightArrow = view.findViewById<ImageButton>(R.id.rightArrow)
        val dayLabel = view.findViewById<TextView>(R.id.dayLabel)
        val viewPager = view.findViewById<ViewPager2>(R.id.itineraryViewPager)

        calendarButton.setOnClickListener {
            val googleAccount = GoogleSignIn.getLastSignedInAccount(requireContext())
            if (googleAccount == null) {
                Log.e("GoogleCalendar", "No Google account signed in")
            }
            if (googleAccount != null) {
                Log.d("GoogleCalendar", "Adding events for account: ${googleAccount.displayName}")
                parsedItineraryViewModel.itinerary.observe(viewLifecycleOwner) { itinerary ->
                    val eventsMap = parsedItineraryViewModel.parseItinerary(itineraryToParse)
                    Log.d("GoogleCalendar", "${itineraryToParse}")
                    parsedItineraryViewModel.addEventsToGoogleCalendar(requireContext(), googleAccount, eventsMap)
                    Toast.makeText(context, "Events added to Google Calendar", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "User not signed in", Toast.LENGTH_SHORT).show()
            }
        }

        backButton = view.findViewById<ImageButton>(R.id.backbutton)
        backButton.setOnClickListener {
            findNavController().navigate(R.id.global_action_to_tripScreen)

        }
    }

    private fun setupViewModel(tripId: Int) {
        viewModel = ViewModelProvider(this).get(ItineraryViewModel::class.java)
        if (tripId != -1) {
            viewModel.getItineraryById(tripId).observe(viewLifecycleOwner) { itinerary ->
                Glide.with(this).load(itinerary.imageUrl).into(imageView)


                val inputFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.US)
                val outputFormat = SimpleDateFormat("MMMM d, yyyy", Locale.US)

                // Convert and set departure date
                val parsedDepartDate = inputFormat.parse(itinerary.departureDate)
                if (parsedDepartDate != null) {
                    departDateTextView.text = outputFormat.format(parsedDepartDate)
                }
                val parsedReturnDate = inputFormat.parse(itinerary.returnDate)
                if (parsedReturnDate != null) {
                    returnDateTextView.text = outputFormat.format(parsedReturnDate)
                }

                departureAirportTextView.text = itinerary.departureAirport
                departureAirport2TextView.text = itinerary.departureAirport2
                arrivalAirportTextView.text = itinerary.arrivalAirport
                arrivalAirport2TextView.text = itinerary.arrivalAirport2
                departTimeTextView.text = itinerary.departureTime
                departTime2TextView.text = itinerary.departureTime2
                arrivalTimeTextView.text = itinerary.arrivalTime
                arrivalTime2TextView.text = itinerary.arrivalTime2
                location.text = itinerary.cityName
                itineraryToParse = itinerary.itineraryDetails

                daysItinerary = parseItineraryToDayItinerary(itinerary.itineraryDetails)

                val itineraryPagerAdapter = ItineraryPagerAdapter(daysItinerary, this)
                itineraryViewPager.adapter = itineraryPagerAdapter

                //excursionAdapter.updateExcursions(itinerary.excursions)
                parsedItineraryViewModel.setItinerary(itinerary.itineraryDetails)

                setupViewPager()

            }
        } else {

        }
    }

    private fun setupViewPager() {
        itineraryViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                dayLabelTextView.text = "Day ${position + 1}"
                updateArrows(position)
                updateDayLabel(position)

            }
        })

        leftArrowButton.setOnClickListener {
            if (itineraryViewPager.currentItem > 0) {
                itineraryViewPager.currentItem -= 1
            }
        }

        rightArrowButton.setOnClickListener {
            if (itineraryViewPager.currentItem < (itineraryViewPager.adapter?.itemCount ?: 1) - 1) {
                itineraryViewPager.currentItem += 1
            }
        }

        updateArrows(itineraryViewPager.currentItem)
    }

    private fun updateArrows(currentPage: Int) {
        leftArrowButton.isEnabled = currentPage > 0
        rightArrowButton.isEnabled = currentPage < (itineraryViewPager.adapter?.itemCount ?: 0) - 1
    }

    private fun updateDayLabel(currentPage: Int) {
        if (daysItinerary.isNotEmpty() && currentPage >= 0 && currentPage < daysItinerary.size) {
            val fullDateString = daysItinerary[currentPage].date.trim()

            val datePart = fullDateString.replace(Regex("Day \\d+:\\s*"), "")

            try {
                val parsedDate = parseDate(datePart)
                val formattedDate = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.US).format(parsedDate)
                dayLabelTextView.text = formattedDate
            } catch (e: ParseException) {
                Log.e("Results", "Error processing date: Unparseable date: '$datePart'")
                dayLabelTextView.text = "Invalid date format"
            }
        } else {
            dayLabelTextView.text = "Day information unavailable"
        }
    }

    private fun parseDate(dateStr: String): Date {
        val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.US)
        return try {
            dateFormat.parse(dateStr)
        } catch (e: ParseException) {
            val yearAddedDateStr = "$dateStr, ${Calendar.getInstance().get(Calendar.YEAR)}"
            dateFormat.parse(yearAddedDateStr) ?: throw ParseException("Unparseable date: $dateStr", 0)
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

    private fun parseItineraryToDayItinerary(itinerary: String): List<DayItinerary> {
        val parsed = mutableMapOf<String, MutableList<Excursion>>()
        val regex = """\(Day \d+: ([^|]+)\|([^|]+)\|?([^)]*)\)""".toRegex()

        for (match in regex.findAll(itinerary)) {
            val date = match.groupValues[1].trim()
            val name = match.groupValues[2].trim()
            val time = match.groupValues[3].trim()

            val excursion = Excursion(name, time)

            if (parsed[date] == null) {
                parsed[date] = mutableListOf(excursion)
            } else {
                parsed[date]!!.add(excursion)
            }
        }


        Log.d("Parsing", "Parsed Itinerary: $parsed")

        return parsed.map { (date, excursions) -> DayItinerary(date, excursions) }
    }
}
