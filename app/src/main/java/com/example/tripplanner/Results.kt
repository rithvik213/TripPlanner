package com.example.tripplanner

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.flightapitest.fetchFlightOffers
import com.google.android.gms.auth.api.signin.GoogleSignIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Results : Fragment() {
    private val cityIataCodes = mapOf(
        "London" to "LON",
        "New York" to "NYC",
        "Tokyo" to "TYO",
        "Paris" to "PAR",
        "Sydney" to "SYD",
        "Los Angeles" to "LAX",
        "Rome" to "ROM",
        "Berlin" to "BER",
        "Beijing" to "BJS",
        "Mumbai" to "BOM",
        "Austin" to "AUS"
    )
    private lateinit var adapter: ExcursionAdapter
    private var excursions: MutableList<Excursion> = mutableListOf()
    private lateinit var viewModel: ExcursionsViewModel
    private lateinit var cityName: String
    private lateinit var departDate: String
    private var budget = 0
    private lateinit var origin: String
    private lateinit var returnDate: String
    private lateinit var chatGPTService: ChatGPTService
    private lateinit var viewPager: ViewPager2
    private var daysItinerary: MutableList<DayItinerary> = mutableListOf()
    private lateinit var database: AppDatabase
    private lateinit var userId: String
    private lateinit var progressBar: ProgressBar
    private lateinit var imageUrl: String
    private var isAttractionsFetched = false
    private var isEventsFetched = false
    private lateinit var viewModelPrefs: SharedViewModel

    private lateinit var departAirport: String
    private lateinit var arrivalAirport: String
    private lateinit var departAirport2: String
    private lateinit var arrivalAirport2: String
    private lateinit var departTime: Date
    private lateinit var arrivalTime: Date
    private lateinit var departTime2: Date
    private lateinit var arrivalTime2: Date
    private lateinit var departTerminal: String
    private lateinit var arrivalTerminal: String
    private lateinit var departTerminal2: String
    private lateinit var arrivalTerminal2: String
    private lateinit var price: String

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
    private val dateFormatter = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.US)
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.US)


    @SuppressLint("ResourceType")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_results, container, false)
        viewModelPrefs = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        initializeUI(view)
        initializeGoogleUser()
        return view
    }

    private fun initializeUI(view: View) {
        progressBar = view.findViewById(R.id.progressBar)
        adapter = ExcursionAdapter(excursions)
        viewPager = view.findViewById(R.id.itineraryViewPager)
        viewPager.adapter = ItineraryPagerAdapter(daysItinerary, this)

        val backButton = view.findViewById<ImageButton>(R.id.resultsbackbutton)
        val saveButton = view.findViewById<Button>(R.id.saveButton)

        backButton.setOnClickListener {
            findNavController().navigate(R.id.action_resultsFragment_to_tripSearchFragment)
        }

        saveButton.setOnClickListener {
            val formattedItinerary = formatItineraryForSaving(daysItinerary)
            if (this::departAirport.isInitialized && this::arrivalAirport.isInitialized) {  // Ensure variables are initialized
                saveItinerary(
                    cityName = cityName,
                    itineraryDetails = formattedItinerary,
                    departureAirport = departAirport,
                    arrivalAirport = arrivalAirport,
                    departureDate = dateFormatter.format(departTime),
                    returnDate = dateFormatter.format(arrivalTime2),
                    departureTime = timeFormatter.format(departTime),
                    arrivalTime = timeFormatter.format(arrivalTime),
                    departureAirport2 = departAirport2,
                    arrivalAirport2 = arrivalAirport2,
                    departureTime2 = timeFormatter.format(departTime2),
                    arrivalTime2 = timeFormatter.format(arrivalTime2),
                    departureTerminal = departTerminal,
                    arrivalTerminal = arrivalTerminal,
                    departureTerminal2 = departTerminal2,
                    arrivalTerminal2 = arrivalTerminal2,
                    totalPrice = price
                )
                findNavController().navigate(R.id.action_resultsFragment_to_homeScreenFragment)
                fetchItineraries()
            } else {
                Toast.makeText(context, "Flight information not fully available", Toast.LENGTH_LONG).show()
            }
        }


        viewModel = ViewModelProvider(requireActivity()).get(ExcursionsViewModel::class.java)
        viewModel.excursions.observe(viewLifecycleOwner) { updateRecyclerView() }

        arguments?.let {
            cityName = it.getString("cityName", "Austin")  //Default to "Austin" if no argument is passed
            departDate = it.getString("departDate","")
            returnDate = it.getString("returnDate", "")
            origin = it.getString("origin", "")
            budget = it.getString("budget", "0").removePrefix("$").toInt()
        }

        chatGPTService = ChatGPTService("OPEN_AI_KEY")
    }

    private fun initializeGoogleUser() {
        val account = GoogleSignIn.getLastSignedInAccount(requireContext())
        if (account != null) {
            userId = account.id ?: ""
        } else {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_LONG).show()
            //findNavController().navigate(R.id.loginFragment)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val app = requireActivity().application as MyApp
        database = app.database
        fetchAttractions(cityName)

        fetchEvents(cityName)

        fetchFlights(view)
        //fetchEvents(cityName)

    }

    private fun updateRecyclerView() {
        adapter.updateExcursions(ArrayList(excursions))
    }


    private fun tryGenerateItinerary() {
        if (isAttractionsFetched && isEventsFetched) {
            generateItinerary()
        }
    }


    private fun fetchFlights(view: View){
        lifecycleScope.launch {
            val flightInfo = fetchFlightOffers(
                originLoc = cityIataCodes[origin]!!,
                destLoc = cityIataCodes[cityName]!!,
                departureDate = departDate,
                returnDate = returnDate,
                adults = 1,
                maxPrice = budget,
                currencyCode = "USD",
                max = 1
            )
            Log.i("originLoc",cityIataCodes[origin]!!)
            Log.i("destLoc",cityIataCodes[cityName]!!)
            Log.i("depdate",departDate)
            Log.i("retdate",returnDate)
            Log.i("budget",budget.toString())

            if (flightInfo == null){
                Toast.makeText(context, "No flights found with given parameters", Toast.LENGTH_LONG).show()
            } else {
                departAirport = flightInfo.flightOffers[0].itineraries[0].segments[0].departure.iataCode
                arrivalAirport = flightInfo.flightOffers[0].itineraries[0].segments[0].arrival.iataCode
                departAirport2 = flightInfo.flightOffers[0].itineraries[1].segments[0].departure.iataCode
                arrivalAirport2 = flightInfo.flightOffers[0].itineraries[1].segments[0].arrival.iataCode
                price = flightInfo.flightOffers[0].price.total

                departTime = dateFormat.parse(flightInfo.flightOffers[0].itineraries[0].segments[0].departure.dateTime)!!
                arrivalTime = dateFormat.parse(flightInfo.flightOffers[0].itineraries[0].segments[0].arrival.dateTime)!!
                departTime2 = dateFormat.parse(flightInfo.flightOffers[0].itineraries[1].segments[0].departure.dateTime)!!
                arrivalTime2 = dateFormat.parse(flightInfo.flightOffers[0].itineraries[1].segments[0].arrival.dateTime)!!
                departTerminal = flightInfo.flightOffers[0].itineraries[0].segments[0].departure.terminal ?: "N/A"
                arrivalTerminal = flightInfo.flightOffers[0].itineraries[0].segments[0].arrival.terminal ?: "N/A"
                departTerminal2 = flightInfo.flightOffers[0].itineraries[1].segments[0].departure.terminal ?: "N/A"
                arrivalTerminal2 = flightInfo.flightOffers[0].itineraries[1].segments[0].arrival.terminal ?: "N/A"

                view.findViewById<TextView>(R.id.destination).text = cityName
                view.findViewById<TextView>(R.id.departureAirportCode).text = departAirport
                view.findViewById<TextView>(R.id.arrivalAirportCode).text = arrivalAirport
                view.findViewById<TextView>(R.id.departureAirportCode2).text = departAirport2
                view.findViewById<TextView>(R.id.arrivalAirportCode2).text = arrivalAirport2
                view.findViewById<TextView>(R.id.departuredate).text = dateFormatter.format(departTime!!)
                view.findViewById<TextView>(R.id.returndates).text = dateFormatter.format(arrivalTime2!!)
                view.findViewById<TextView>(R.id.departureTime).text = timeFormatter.format(departTime)
                view.findViewById<TextView>(R.id.arrivalTime).text = timeFormatter.format(arrivalTime!!)
                view.findViewById<TextView>(R.id.departureTime2).text = timeFormatter.format(departTime2!!)
                view.findViewById<TextView>(R.id.arrivalTime2).text = timeFormatter.format(arrivalTime2)
                view.findViewById<TextView>(R.id.departureTerminal).text = "Terminal " + departTerminal
                view.findViewById<TextView>(R.id.arrivalTerminal).text = "Terminal " + arrivalTerminal
                view.findViewById<TextView>(R.id.departureTerminal2).text = "Terminal " + departTerminal2
                view.findViewById<TextView>(R.id.arrivalTerminal2).text = "Terminal " + arrivalTerminal2
                view.findViewById<TextView>(R.id.totalprice).text = "$" + price
            }

        }

    }

    private fun fetchAttractions(cityName: String) {
        val tripAdvisorManager = TripAdvisorManager(
            requireContext(),
            cityName,
            object : TripAdvisorManager.AttractionFetchListener {
                override fun onAttractionsFetched(attractions: List<String>) {
                    val newExcursions = attractions.map { attraction ->
                        Excursion(attraction, "All Day")
                    }
                    activity?.runOnUiThread {
                        excursions.addAll(newExcursions)
                        //updateRecyclerView()
                        viewModel.addExcursions(newExcursions)
                        //generateItinerary()
                        isAttractionsFetched = true
                        tryGenerateItinerary()
                    }
                }

                override fun onAttractionFetchFailed(errorMessage: String) {
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                    isAttractionsFetched = true
                    tryGenerateItinerary()
                }
            },
            object : TripAdvisorManager.ImageFetchListener {
                override fun onImageFetched(imageUrl: String) {
                    this@Results.imageUrl = imageUrl
                }

                override fun onImageFetchFailed(errorMessage: String) {
                    Toast.makeText(context, "Image fetch failed: $errorMessage", Toast.LENGTH_LONG).show()
                    imageUrl = "default_image_url"
                }

        }
        )
        tripAdvisorManager.fetchData()
    }

    private fun fetchEvents(cityName: String) {
        val eventFetcher = EventFetcher(cityName, requireContext(), departDate, returnDate, object : EventFetcher.EventFetchListener {
            override fun onEventsFetched(events: List<EventFetcher.EventResult>) {
                val eventsExcursions = events.map { event ->
                    Excursion(event.title, event.date.`when`)
                }
                //Toast.makeText(context, "TEST", Toast.LENGTH_LONG).show()

                activity?.runOnUiThread {
                    excursions.addAll(eventsExcursions)
                    //updateRecyclerView()
                    //viewModel.addExcursions(eventsExcursions)
                    //generateItinerary()
                    isEventsFetched = true
                    tryGenerateItinerary()
                }
            }

            override fun onEventFetchFailed(errorMessage: String) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                isEventsFetched = true
                tryGenerateItinerary()
            }
        })
        eventFetcher.fetchEvents()
    }

    private fun generateItinerary() {
        if (excursions.isNotEmpty()) {
            showLoading(true)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val prompt = buildItineraryPrompt()
                    val itinerary = chatGPTService.generateResponse(prompt)
                    val parsedItineraries = parseItinerary(itinerary)
                    CoroutineScope(Dispatchers.Main).launch {
                        (viewPager.adapter as ItineraryPagerAdapter).apply {
                            daysItinerary.clear()
                            daysItinerary.addAll(parsedItineraries)
                            notifyDataSetChanged()
                        }
                        Log.d("ResultsFragment", "Itinerary updated in ViewPager")
                        showLoading(false)
                    }
                } catch (e: Exception) {
                    Log.e("ResultsFragment", "Failed to generate itinerary", e)
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(context, "Failed to generate itinerary: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        showLoading(false)
                    }
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun buildItineraryPrompt(): String {
        val itineraryBuilder = StringBuilder()
        val selectedAttractions = viewModelPrefs.selectedAttractions.value?.joinToString(separator = ", ") { it }
        itineraryBuilder.append("Generate a detailed day-by-day itinerary for a trip to $cityName from $departDate to $returnDate with the following attractions only including attractions from this list if they match the dates of the trip while prioritizing attractions that include $selectedAttractions:\n")

        for (excursion in excursions) {
            itineraryBuilder.append("- ${excursion.name}")


            itineraryBuilder.append(" at ${excursion.time}")

            itineraryBuilder.append("\n")
        }

        val sampleItinerary = "Day 1: Morning Breakfast on Sun Apr 21, 11am-12pm, Jim Gaffigan show at the Sun Theater on Sun Apr 21, 4pm-9pm. Each event in the itinerary must include its name, location if possible and the length 7:00pm-8:00pm like that. Events must happen sequentially and not overlap. Here is a sample for one day detailed itinerary. I need it like this for all days." +
                " - Breakfast at hotel\n" +
                " - 10:00am - Visit the Isabella Stewart Gardner Museum\n" +
                " - 12:00pm - Lunch at a local cafe\n" +
                " - 2:00pm - Walk around the Boston Public Garden\n" +
                " - 4:00pm - Rest and relax at hotel\n" +
                " - 7:00pm - Jim Gaffigan at the Wilbur Theatre\n" +
                " - 9:30pm - Dinner at a nearby restaurant\n" +
                " - 11:00pm - Return to hotel for the night"
        return itineraryBuilder.toString() +
                "You should output a formatted itinerary like this as a sample. $sampleItinerary"
    }

    private fun parseItinerary(itineraryString: String): List<DayItinerary> {
        val days = itineraryString.split(Regex("(?=Day \\d+:)"))
            .filter { it.isNotBlank() }

        return days.map { dayInfo ->
            val lines = dayInfo.trim().split("\n")
            val date = lines.first().trim()

            val activities = lines.drop(1) // Drop the date line
                .filter { it.startsWith("- ") }
                .map { activity ->
                    val detail = activity.drop(2).trim() // Remove the leading "- "
                    val timeIndex = detail.lastIndexOf(" - ")
                    if (timeIndex > -1) {
                        val name = detail.substring(0, timeIndex).trim()
                        val time = detail.substring(timeIndex + 3).trim()
                        Excursion(name, time)
                    } else {
                        Excursion(detail, "")
                    }
                }

            DayItinerary(date, activities)
        }
    }

    private fun formatItineraryForSaving(daysItinerary: List<DayItinerary>): String {
        return daysItinerary.joinToString(separator = "\n") { day ->
            "Date: ${day.date}\nExcursions: ${day.excursions.joinToString(separator = ", ") { it.name }}"
        }
    }

    fun saveItinerary(
        cityName: String,
        itineraryDetails: String,
        departureAirport: String,
        arrivalAirport: String,
        departureDate: String,
        returnDate: String,
        departureTime: String,
        arrivalTime: String,
        departureAirport2: String?,
        arrivalAirport2: String?,
        departureTime2: String?,
        arrivalTime2: String?,
        departureTerminal: String,
        arrivalTerminal: String,
        departureTerminal2: String?,
        arrivalTerminal2: String?,
        totalPrice: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val itinerary = Itinerary(
                    userId = userId,
                    tripDates = "$departDate to $returnDate",
                    cityName = cityName,
                    itineraryDetails = itineraryDetails,
                    imageUrl = imageUrl,
                    departureAirport = departureAirport,
                    arrivalAirport = arrivalAirport,
                    departureDate = departureDate,
                    returnDate = returnDate,
                    departureTime = departureTime,
                    arrivalTime = arrivalTime,
                    departureAirport2 = departureAirport2,
                    arrivalAirport2 = arrivalAirport2,
                    departureTime2 = departureTime2,
                    arrivalTime2 = arrivalTime2,
                    departureTerminal = departureTerminal,
                    arrivalTerminal = arrivalTerminal,
                    departureTerminal2 = departureTerminal2,
                    arrivalTerminal2 = arrivalTerminal2,
                    totalPrice = totalPrice
                )
                database.itineraryDao().insertItinerary(itinerary)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Itinerary saved successfully", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to save itinerary: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    fun fetchItineraries() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val itineraries = database.itineraryDao().getAllItineraries()
                itineraries.forEach {
                    Log.d("FetchData", "Itinerary: ${it.cityName}, Dates: ${it.tripDates}")
                }
            } catch (e: Exception) {
                Log.e("FetchData", "Error fetching itineraries", e)
            }
        }
    }



}
