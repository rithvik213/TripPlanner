package com.example.tripplanner

import android.annotation.SuppressLint
import android.os.Bundle
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

class Results : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ExcursionAdapter
    private var excursions: MutableList<Excursion> = mutableListOf<Excursion>()
    private lateinit var viewModel: ExcursionsViewModel
    private lateinit var cityName: String

    @SuppressLint("ResourceType")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_results, container, false)

        val backButton = view.findViewById<ImageButton>(R.id.resultsbackbutton)
        val saveButton = view.findViewById<Button>(R.id.saveButton)

        backButton.setOnClickListener {
            findNavController().navigate(R.id.action_resultsFragment_to_tripSearchFragment)
        }

        saveButton.setOnClickListener {
            findNavController().navigate(R.id.action_resultsFragment_to_homeScreenFragment)
        }

        //viewModel = ViewModelProvider(this).get(ExcursionsViewModel::class.java)
        viewModel = ViewModelProvider(requireActivity()).get(ExcursionsViewModel::class.java)


        arguments?.let {
            cityName = it.getString("cityName", "Austin")  // Default to "Austin" if no argument is passed
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.excursionRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(activity)

        adapter = ExcursionAdapter(excursions)
        recyclerView.adapter = adapter

        viewModel.excursions.observe(viewLifecycleOwner) { excursions ->
            adapter.updateExcursions(ArrayList(excursions))
        }

        fetchAttractions("Austin")
        fetchEvents("Austin")
/*
        excursions = listOf(
            Excursion("Beach Volleyball", "10:00 AM"),
            Excursion("City Tour", "11:00 AM"),
        )
*/


    }

    private fun updateRecyclerView() {
        adapter.updateExcursions(ArrayList(excursions))  // Pass a copy if modification concurrency is concerned
    }

    private fun fetchAttractions(cityName: String) {
        val tripAdvisorManager = TripAdvisorManager(
            requireContext(),
            cityName,
            object : TripAdvisorManager.AttractionFetchListener {
                override fun onAttractionsFetched(attractions: List<String>) {
                    //val attractionsString = attractions.joinToString()
                    //Toast.makeText(context, attractionsString, Toast.LENGTH_LONG).show()
                    //maybeBuildItinerary
                    val newExcursions = attractions.map { attraction ->
                        Excursion(attraction, "12:00")  // Use a function to get random time if needed
                    }
                    activity?.runOnUiThread {
                        excursions.addAll(newExcursions)  // Add to the existing list
                        updateRecyclerView()
                        viewModel.addExcursions(newExcursions)
                    }

                    updateRecyclerView()
                }

                override fun onAttractionFetchFailed(errorMessage: String) {
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
        )
        tripAdvisorManager.fetchData()
    }

    private fun fetchEvents(cityName: String) {
        val eventFetcher =
            EventFetcher(cityName, requireContext(), object : EventFetcher.EventFetchListener {
                override fun onEventsFetched(events: List<EventFetcher.EventResult>) {
                    /*var eventsString = events.joinToString(separator = ", ") { event ->
                        "${event.title} ${event.date.`when`}"
                    }
                    Toast.makeText(
                        context,
                        eventsString,
                        Toast.LENGTH_SHORT
                    ).show()
*/
                    var eventsExcursions = events.map { event ->
                        Excursion(event.title, event.date.`when`)
                    }
                    activity?.runOnUiThread {
                        excursions.addAll(eventsExcursions)  // Add to the existing list
                        updateRecyclerView()
                        viewModel.addExcursions(eventsExcursions)
                    }
                    updateRecyclerView()
                    //maybeBuildItinerary()
                }

                override fun onEventFetchFailed(errorMessage: String) {
                    // This is called when there is an error fetching events
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                }
            })
        eventFetcher.fetchEvents()
    }

}
