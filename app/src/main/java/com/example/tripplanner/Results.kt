package com.example.tripplanner

import android.annotation.SuppressLint
import android.app.AlertDialog
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
import com.example.tripplanner.adapters.ExcursionAdapter
import com.example.tripplanner.adapters.ItineraryPagerAdapter
import com.example.tripplanner.apis.openai.ChatGPTService
import com.example.tripplanner.apis.tripadvisor.EventFetcher
import com.example.tripplanner.apis.tripadvisor.TripAdvisorManager
import com.example.tripplanner.data.DayItinerary
import com.example.tripplanner.data.Excursion
import com.example.tripplanner.data.Itinerary

import com.example.tripplanner.database.AppDatabase
import com.example.tripplanner.database.MyApp
import com.example.tripplanner.viewmodels.ExcursionsViewModel
import com.example.tripplanner.viewmodels.SharedViewModel

import com.google.android.gms.auth.api.signin.GoogleSignIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.lang3.time.DateUtils
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Our fragment to display all of the initial flight information and generated itinerary
class Results : Fragment() {
    private lateinit var adapter: ExcursionAdapter
    private var excursions: MutableList<Excursion> = mutableListOf()
    private lateinit var viewModel: ExcursionsViewModel
    private lateinit var cityName: String
    private lateinit var departDate: String
    private lateinit var latLong: String
    private var budget = 0
    private lateinit var origin: String
    private lateinit var destination: String
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

    //Date and Time formatters for time and date of departure
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
    private val dateFormatter = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.US)
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.US)

    private lateinit var leftArrowButton: ImageButton
    private lateinit var rightArrowButton: ImageButton
    private lateinit var dayLabelTextView: TextView

    private var loadingDialog: AlertDialog? = null

    private var isItineraryGenerated = false



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
        dayLabelTextView = view.findViewById(R.id.dayLabel)

        leftArrowButton = view.findViewById(R.id.leftArrow)
        rightArrowButton = view.findViewById(R.id.rightArrow)

        //Move left and right through itinerary
        leftArrowButton.setOnClickListener {
            val currentItem = viewPager.currentItem
            if (currentItem > 0) {
                viewPager.currentItem = currentItem - 1
            }
        }

        rightArrowButton.setOnClickListener {
            val currentItem = viewPager.currentItem
            if (currentItem < adapter.itemCount - 1) {
                viewPager.currentItem = currentItem + 1
            }
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateNavigationButtons()
                updateDayLabel(position)
            }
        })


        val backButton = view.findViewById<ImageButton>(R.id.resultsbackbutton)
        val saveButton = view.findViewById<Button>(R.id.saveButton)

        backButton.setOnClickListener {
            findNavController().navigate(R.id.action_resultsFragment_to_tripSearchFragment)
        }

        //Save all relevant information to the database once user has approved it
        //This is so we can display it later on the 'My Trips' page
        saveButton.setOnClickListener {
            val formattedItinerary = formatItineraryForSaving(daysItinerary) // turns the list of excursions into a string
            if (this::departAirport.isInitialized && this::arrivalAirport.isInitialized) {
                saveItinerary(
                    cityName = cityName,
                    itineraryDetails = formattedItinerary,
                    departureAirport = departAirport,
                    imageURL = imageUrl,
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
                    totalPrice = price,
                    lat_long = latLong
                )
                viewModelPrefs.resetViewModel()
                findNavController().navigate(R.id.action_resultsFragment_to_homeScreenFragment)
                fetchItineraries()
            } else {
                Toast.makeText(context, "Flight information not fully available", Toast.LENGTH_LONG).show()
            }

        }

        viewModel = ViewModelProvider(requireActivity()).get(ExcursionsViewModel::class.java)
        viewModel.excursions.observe(viewLifecycleOwner) { updateRecyclerView() }

        //Unpack bundle from flight_results fragment
        arguments?.let {
            cityName = it.getString("cityName", "Austin")
            departDate = it.getString("departDate","")
            returnDate = it.getString("returnDate", "")
            origin = it.getString("origin", "")
            destination = it.getString("destination", "")
            budget = it.getString("budget", "0").removePrefix("$").toInt()
            latLong = it.getString("latLong", "0, 0")
            Log.i("latLong", latLong)
        }

        chatGPTService = ChatGPTService("OPEN_AI_KEY")
        leftArrowButton.isEnabled = false
        rightArrowButton.isEnabled = false
    }


    //Update label with the date information based on the current page index and a list of itinerary days
    private fun updateDayLabel(currentPage: Int) {
        if (daysItinerary.isNotEmpty() && currentPage >= 0 && currentPage < daysItinerary.size) {
            //Extract the date part from the itinerary for the current page
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

    //Parse date with java's simpleDateFormat
    private fun parseDate(dateStr: String): Date {
        val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.US)
        return try {
            dateFormat.parse(dateStr)!!
        } catch (e: ParseException) {
            //Try to fix ParseException by adding year retroactively
            val yearAddedDateStr = "$dateStr, ${Calendar.getInstance().get(Calendar.YEAR)}"
            dateFormat.parse(yearAddedDateStr) ?: throw ParseException("Unparseable date: $dateStr", 0)
        }
    }

    //Enable or disable update buttons depending on position in itinerary
    private fun updateNavigationButtons() {
        leftArrowButton.isEnabled = viewPager.currentItem > 0
        rightArrowButton.isEnabled = viewPager.currentItem < daysItinerary.size - 1
    }

    //Show the loading dialog and prevent user from interacting with app while loading
    private fun showLoadingDialog(message: String) {
        if (loadingDialog == null) {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.results_itinerary_loading, null)
            loadingDialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create()
        }
        val textView = loadingDialog?.findViewById<TextView>(R.id.userpromptname)
        textView?.text = message

        Log.d("LoadingDialog", "Updating dialog message to: $message")

        loadingDialog?.show()
    }

    private fun dismissLoadingDialog() {
        loadingDialog?.dismiss()
    }
    //Initialize the user so that we can save trip to database under their ID
    private fun initializeGoogleUser() {
        val account = GoogleSignIn.getLastSignedInAccount(requireContext())
        if (account != null) {
            userId = account.id ?: ""
        } else {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_LONG).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val app = requireActivity().application as MyApp
        database = app.database

        val tripAdvisorManager = TripAdvisorManager()

        //Get images for the database with a coroutine so we don't bog down the main thread
        lifecycleScope.launch {
            try {
                val fetchedImageUrl = tripAdvisorManager.fetchCityImage(cityName)
                imageUrl = fetchedImageUrl
            } catch (e: Exception) {
                Log.e("ImageFetch", "Failed to fetch image URL: ${e.message}")
            }
        }

        //Fetch the 3 main things we need to display
        fetchFlights(view)
        fetchAttractions(cityName)
        fetchEvents(cityName)
    }

    private fun updateRecyclerView() {
        adapter.updateExcursions(ArrayList(excursions))
    }

    //Only generates the full itinerary once calls to both SerpAPI and TripAdvisor have been made
    private fun tryGenerateItinerary() {
        Log.d("ResultsFragment", "Attempting to generate itinerary. Attractions Fetched: $isAttractionsFetched, Events Fetched: $isEventsFetched")
        synchronized(this) {
            if (isAttractionsFetched && isEventsFetched && !isItineraryGenerated) {
                isItineraryGenerated = true
                generateItinerary()
            } else {
                Log.d("ResultsFragment", "Cannot generate itinerary yet. Waiting for more data or already generated.")
            }
        }
    }


    //Fetch all the flights info from bundle and populate UI (not visible in current version)
    private fun fetchFlights(view: View){
        val bundle = requireArguments()
        val cityName = bundle.getString("cityName")
        departAirport = bundle.getString("departure.iataCode")!!
        arrivalAirport = bundle.getString("arrival.iataCode")!!
        departAirport2 = bundle.getString("departure2.iataCode")!!
        arrivalAirport2 = bundle.getString("arrival2.iataCode")!!
        departTime = dateFormat.parse(bundle.getString("departure.dateTime")!!)!!
        arrivalTime = dateFormat.parse(bundle.getString("arrival.dateTime")!!)!!
        departTime2 = dateFormat.parse(bundle.getString("departure2.dateTime")!!)!!
        arrivalTime2 = dateFormat.parse(bundle.getString("arrival2.dateTime")!!)!!
        departTerminal = bundle.getString("departure.terminal") ?: "N/A"
        arrivalTerminal = bundle.getString("arrival.terminal") ?: "N/A"
        departTerminal2 = bundle.getString("departure2.terminal") ?: "N/A"
        arrivalTerminal2 = bundle.getString("arrival2.terminal") ?: "N/A"
        price = bundle.getString("price.total")!!

        view.findViewById<TextView>(R.id.destination).text = cityName
        view.findViewById<TextView>(R.id.departureAirportCode).text = departAirport
        view.findViewById<TextView>(R.id.arrivalAirportCode).text = arrivalAirport
        view.findViewById<TextView>(R.id.departureAirportCode2).text = departAirport2
        view.findViewById<TextView>(R.id.arrivalAirportCode2).text = arrivalAirport2

        //Use dateformatter for dates and timeformatter for times since time values come with dates
        view.findViewById<TextView>(R.id.departuredate).text = dateFormatter.format(departTime)
        view.findViewById<TextView>(R.id.returndates).text = dateFormatter.format(arrivalTime2)
        view.findViewById<TextView>(R.id.departureTime).text = timeFormatter.format(departTime)
        view.findViewById<TextView>(R.id.arrivalTime).text = timeFormatter.format(arrivalTime)
        view.findViewById<TextView>(R.id.departureTime2).text = timeFormatter.format(departTime2)
        view.findViewById<TextView>(R.id.arrivalTime2).text = timeFormatter.format(arrivalTime2)

        view.findViewById<TextView>(R.id.departureTerminal).text = "Terminal $departTerminal"
        view.findViewById<TextView>(R.id.arrivalTerminal).text = "Terminal $arrivalTerminal"
        view.findViewById<TextView>(R.id.departureTerminal2).text = "Terminal $departTerminal2"
        view.findViewById<TextView>(R.id.arrivalTerminal2).text = "Terminal $arrivalTerminal2"
        view.findViewById<TextView>(R.id.totalprice).text = "$$price"

    }

    //Get the attractions within the chosen city from TripAdvisor API
    private fun fetchAttractions(cityName: String) {
        val tripAdvisorManager = TripAdvisorManager()
        showLoadingDialog("Please wait as we generate your itinerary...")

        //Prepare the listener implementation to handle fetched data or errors
        val listener = object : TripAdvisorManager.AttractionFetchListener {
            override fun onAttractionsFetched(attractions: List<TripAdvisorManager.AttractionDetail>) {
                lifecycleScope.launch(Dispatchers.Main) {
                    val newExcursions = attractions.map { detail ->
                        Excursion(
                            name = detail.name,
                            time = "Can Be Any Time",
                            imageUrl = detail.imageUrl ?: "default_image_url"
                        )
                    }
                    //Add all our excursions/attractions and then try to generate the itinerary
                    excursions.addAll(newExcursions)
                    viewModel.addExcursions(newExcursions)
                    isAttractionsFetched = true
                    tryGenerateItinerary()
                }
            }

            override fun onAttractionFetchFailed(errorMessage: String) {
                lifecycleScope.launch(Dispatchers.Main) {
                    //Also try to generate itinerary on failure, but make sure to notify first
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                    dismissLoadingDialog()
                    isAttractionsFetched = true
                    tryGenerateItinerary()
                }
            }
        }

        //Make the call to fetchData providing the listener
        tripAdvisorManager.fetchData(requireContext(), "things to do near $cityName", null, "attractions", listener)
    }



    //Fetches events from SerpAPI and turns them into type excursions
    private fun fetchEvents(cityName: String) {
        Log.d("ResultsFragment", "Starting to fetch events.")

        //Need a number of date formats because SerpAPI could be in any of them
        val possibleDateFormats = arrayOf(
            "yyyy-MM-dd",        //Standard ISO format
            "EEE, MMM dd",       //Weekday + Month + Date (Sun, May 26)
            "MMM dd",            //Month + date (May 26)
            "EEE, MMM dd, yyyy", //Full date with year
            "MM/dd/yyyy"         //US standard format
        )

        //Parse our known input dates
        val startDate = DateUtils.parseDate(departDate, "yyyy-MM-dd").also {
            Log.d("ResultsFragment", "Parsed start date: $it")
        }

        val endDate = DateUtils.parseDate(returnDate, "yyyy-MM-dd").also {
            Log.d("ResultsFragment", "Parsed end date: $it")
        }

        try {
            val eventFetcher = EventFetcher(
                cityName,
                requireContext(),
                departDate,
                returnDate,
                object : EventFetcher.EventFetchListener {
                    override fun onEventsFetched(events: List<EventFetcher.EventResult>) {
                        Log.d("ResultsFragment", "Processing ${events.size} events.")
                        val eventsExcursions = events.filter { event ->
                            val dateString = extractDatePart(event.date.`when`)
                            Log.d("ResultsFragment", "Processing event date: ${event.date.`when`} extracted as: $dateString")
                            //Try parsing our returned eventDate using our many formats and skip if none work
                            val eventDate = dateString?.let {
                                try {
                                    DateUtils.parseDate(it, *possibleDateFormats)
                                } catch (e: ParseException) {
                                    Log.d("ResultsFragment", "Skipping event with invalid date format: $it")
                                    null
                                }
                            }
                            //Filter the event results according to our trip dates
                            (eventDate != null && !eventDate.before(startDate) && !eventDate.after(endDate)).also {
                                if (it) Log.d("ResultsFragment", "Included event: ${event.title} on $dateString")
                                else Log.d("ResultsFragment", "Excluded event: ${event.title} on $dateString")
                            }
                        }.map { event ->
                            Excursion(event.title, event.date.`when`)
                        }

                        //Add excursions and try to generate itinerary once finished
                        activity?.runOnUiThread {
                            excursions.addAll(eventsExcursions)
                            viewModel.addExcursions(eventsExcursions)
                            isEventsFetched = true
                            tryGenerateItinerary()
                        }
                    }
                    //On failure, show to user and still try to generate itinerary
                    override fun onEventFetchFailed(errorMessage: String) {
                        Log.e("ResultsFragment", "Error fetching events: $errorMessage")
                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                        isEventsFetched = true
                        tryGenerateItinerary()
                    }
                })
            eventFetcher.fetchEvents()
        } catch (e: Exception) {
            Log.e("ResultsFragment", "Exception while fetching events: ${e.message}", e)
            isEventsFetched = true
            tryGenerateItinerary()
        }
    }

    //Extract dates from the given string in two possible formats ("MMM d" or "EEE, MMM d")
    fun extractDatePart(dateStr: String): String? {
        val regex = Regex(
            pattern = """(\w{3}, \w{3} \d{1,2})|(\w{3} \d{1,2})""",
            options = setOf(RegexOption.IGNORE_CASE)
        )
        return regex.find(dateStr)?.value
    }


    //Itinerary generation based on all the excursions added by SerpAPI and TripAdvisor
    private fun generateItinerary() {
        Log.d("ResultsFragment", "Generating itinerary with ${excursions.size} excursions")
        if (excursions.isNotEmpty()) {
            Log.d("ResultsFragment", "Starting itinerary generation with ${excursions.size} excursions")
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    //Generate prompt for ChatGPT
                    val prompt = buildItineraryPrompt()
                    Log.d("ResultsFragment", "Itinerary Prompt: $prompt")
                    //Get the response from chatGPT
                    val itinerary = chatGPTService.generateResponse(prompt)
                    Log.d("ResultsFragment", "Generated Itinerary Text: $itinerary")
                    //Turn this back into a list for the recyclerView
                    val parsedItineraries = parseItinerary(itinerary)
                    Log.d("ResultsFragment", "Parsed Itineraries: ${parsedItineraries.size} days")

                    CoroutineScope(Dispatchers.Main).launch {
                        (viewPager.adapter as ItineraryPagerAdapter).apply {
                            daysItinerary.clear()
                            daysItinerary.addAll(parsedItineraries)
                            notifyDataSetChanged()
                        }
                        updateDayLabel(viewPager.currentItem)
                        updateNavigationButtons()
                        Log.d("ResultsFragment", "Itinerary updated in ViewPager")
                        dismissLoadingDialog()
                    }
                } catch (e: Exception) {
                    Log.e("ResultsFragment", "Failed to generate itinerary", e)
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(context, "Failed to generate itinerary: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        } else {
            Log.d("ResultsFragment", "No excursions available to generate itinerary")
        }
    }

    //Create the prompt we feed into ChatGPT in order to generate the itinerary
    private fun buildItineraryPrompt(): String {
        val itineraryBuilder = StringBuilder()

        //This is the check box buttons selected on trip_search fragment
        val selectedAttractions = viewModelPrefs.selectedAttractions.value?.joinToString(separator = ", ") { it }
        Log.d("BuildItinerary", "City: $cityName, Departure: $departDate, Return: $returnDate, Attractions: $selectedAttractions")
        itineraryBuilder.append("Generate a detailed day-by-day itinerary for a trip to $cityName from $departDate to $returnDate with the following attractions only including attractions from this list if they match the dates of the trip while prioritizing attractions that include the following types $selectedAttractions:\n")

        for (excursion in excursions) {
            Log.d("BuildItinerary", "Processing Excursion: ${excursion.name} at ${excursion.time}")

            itineraryBuilder.append("- ${excursion.name}")
            itineraryBuilder.append(" at ${excursion.time}")
            itineraryBuilder.append("\n")
        }

        //Must be very specific with chatGPT so we can then get the response necessary for parsing into necessary data class
        val sampleItinerary = "Day 1: Morning Breakfast on Sun Apr 21, 11am-12pm, Jim Gaffigan show at the Sun Theater on Sun Apr 21, 4pm-9pm. Each event in the itinerary must include its name, location if possible and the length 7:00pm-8:00pm like that. Events must happen sequentially and not overlap. Here is a sample for one day detailed itinerary. I need it like this for all days." +
                " - 10:00am - Visit the Isabella Stewart Gardner Museum\n" +
                " - 12:00pm - Lunch at a local cafe\n" +
                " - 2:00pm - Walk around the Boston Public Garden\n" +
                " - 4:00pm - Rest and relax at hotel\n" +
                " - 7:00pm - Jim Gaffigan at the Wilbur Theatre\n" +
                " - 9:30pm - Dinner at a nearby restaurant\n" +
                " - 11:00pm - Return to hotel for the night"

        Log.d("BuildItinerary", "Final Itinerary: $itineraryBuilder")
        return itineraryBuilder.toString() +
                "You should output a formatted itinerary like this as a sample. $sampleItinerary"
    }

    //Based on the exact format for the itinerary specified to be responded with by the ChatGPT prompt
    private fun parseItinerary(itineraryString: String): List<DayItinerary> {
        //Split the itinerary string into individual days
        val days = itineraryString.split(Regex("(?=Day \\d+:)"))
            //Filter out blank lines
            .filter { it.isNotBlank() }
        //Map each day section to a DayItinerary object
        return days.map { dayInfo ->
            //Split the day section into lines and trim whitespace
            val lines = dayInfo.trim().split("\n")
            //Extract date
            val date = lines.first().trim()
            val activities = lines.drop(1)
                //Filter lines starting with "- ".
                .filter { it.startsWith("- ") }
                //Map each activity line to an Excursion object.
                .map { activity ->
                    //Remove the hyphens and trim
                    val detail = activity.drop(2).trim()
                    //Find where " - " is to separate activity name and time.
                    val timeIndex = detail.lastIndexOf(" - ")
                    if (timeIndex > -1) {
                        //Get activity name and time
                        val name = detail.substring(0, timeIndex).trim()
                        val time = detail.substring(timeIndex + 3).trim()
                        Excursion(name, time)
                    } else {
                        //If there isn't a time is specified it can be empty
                        Excursion(detail, "")
                    }
                }

            //Create and return the DayItinerary
            DayItinerary(date, activities)
        }
    }


    //Turning the itinerary into a string to save into the database that can also then be easily parsed again later
    private fun formatItineraryForSaving(daysItinerary: List<DayItinerary>): String {
        return daysItinerary.joinToString(separator = "\n") { day ->
            day.excursions.joinToString(separator = "") {"(" + day.date + "|" + it.time + "|" + it.name + ")"}
        }
    }

    //Save the itinerary to our database using a coroutine
    fun saveItinerary(
        cityName: String,
        itineraryDetails: String,
        departureAirport: String,
        imageURL: String,
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
        totalPrice: String,
        lat_long: String
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
                    totalPrice = totalPrice,
                    latLong = lat_long
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
