package com.example.tripplanner

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Results : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ExcursionAdapter
    private var excursions: MutableList<Excursion> = mutableListOf()
    private lateinit var viewModel: ExcursionsViewModel
    private lateinit var cityName: String
    private lateinit var departDate: String
    private lateinit var returnDate: String
    private lateinit var chatGPTService: ChatGPTService
    private lateinit var viewPager: ViewPager2
    private var daysItinerary: MutableList<DayItinerary> = mutableListOf()

    @SuppressLint("ResourceType")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_results, container, false)
        initializeUI(view)
        return view
    }

    private fun initializeUI(view: View) {
        //recyclerView = view.findViewById(R.id.excursionRecyclerView)
        //recyclerView.layoutManager = LinearLayoutManager(activity)
        adapter = ExcursionAdapter(excursions)
        //recyclerView.adapter = adapter
        viewPager = view.findViewById(R.id.itineraryViewPager)
        viewPager.adapter = ItineraryPagerAdapter(daysItinerary, this)

        val backButton = view.findViewById<ImageButton>(R.id.resultsbackbutton)
        val saveButton = view.findViewById<Button>(R.id.saveButton)

        backButton.setOnClickListener {
            findNavController().navigate(R.id.action_resultsFragment_to_tripSearchFragment)
        }

        saveButton.setOnClickListener {
            findNavController().navigate(R.id.action_resultsFragment_to_homeScreenFragment)
        }

        viewModel = ViewModelProvider(requireActivity()).get(ExcursionsViewModel::class.java)
        viewModel.excursions.observe(viewLifecycleOwner) { updateRecyclerView() }

        arguments?.let {
            cityName = it.getString("cityName", "Austin")  //Default to "Austin" if no argument is passed
            departDate = it.getString("departDate","")
            returnDate = it.getString("returnDate", "")
        }

        chatGPTService = ChatGPTService("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")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchAttractions(cityName)
        //fetchEvents(cityName)
    }

    private fun updateRecyclerView() {
        adapter.updateExcursions(ArrayList(excursions))  // Refresh data in adapter
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
                        generateItinerary()
                    }
                }

                override fun onAttractionFetchFailed(errorMessage: String) {
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
        )
        tripAdvisorManager.fetchData()
    }

    private fun fetchEvents(cityName: String) {
        val eventFetcher = EventFetcher(cityName, requireContext(), object : EventFetcher.EventFetchListener {
            override fun onEventsFetched(events: List<EventFetcher.EventResult>) {
                val eventsExcursions = events.map { event ->
                    Excursion(event.title, event.date.`when`)
                }
                activity?.runOnUiThread {
                    excursions.addAll(eventsExcursions)
                    updateRecyclerView()
                    viewModel.addExcursions(eventsExcursions)
                    generateItinerary()
                }
            }

            override fun onEventFetchFailed(errorMessage: String) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            }
        })
        eventFetcher.fetchEvents()
    }

    private fun generateItinerary() {
        if (excursions.isNotEmpty()) {
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
                    }
                } catch (e: Exception) {
                    Log.e("ResultsFragment", "Failed to generate itinerary", e)
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(context, "Failed to generate itinerary: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }



    private fun buildItineraryPrompt(): String {
        val itineraryBuilder = StringBuilder()
        itineraryBuilder.append("Generate a detailed day-by-day itinerary for a trip to $cityName from $departDate to $returnDate with the following attractions only including attractions from this list if they match the dates of the trip:\n")

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
            .filter { it.isNotBlank() } // Ensure we don't take any empty strings from split artifacts

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

}
