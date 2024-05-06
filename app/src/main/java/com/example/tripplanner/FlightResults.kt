package com.example.tripplanner

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tripplanner.adapters.FlightAdapter
import com.example.tripplanner.apis.amadeus.fetchFlightOffers
import kotlinx.coroutines.launch


class FlightResults : Fragment() {
    private lateinit var cityName: String
    private lateinit var departDate: String
    private var budget = 0
    private lateinit var origin: String
    private lateinit var destination: String
    private lateinit var returnDate: String
    private lateinit var latLong: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_flight_results, container, false)
        arguments?.let {
            cityName = it.getString("cityName", "Austin")
            departDate = it.getString("departDate","")
            returnDate = it.getString("returnDate", "")
            origin = it.getString("origin", "")
            destination = it.getString("destination", "")
            budget = it.getString("budget", "0").removePrefix("$").toInt()
            latLong = it.getString("latLong", "0, 0")
        }

        val flightsRecyclerView: RecyclerView = view.findViewById(R.id.flightReyclerView)

        val adapter = FlightAdapter(emptyList())
        flightsRecyclerView.adapter = adapter


        val layoutManager = LinearLayoutManager(requireContext())
        flightsRecyclerView.layoutManager = layoutManager

        lifecycleScope.launch {
            val flightInfo = fetchFlightOffers(
                originLoc = origin,
                destLoc = destination,
                departureDate = departDate,
                returnDate = returnDate,
                adults = 1,
                maxPrice = budget,
                currencyCode = "USD",
                max = 20
            )
            if (flightInfo != null) {
                adapter.updateData(flightInfo.flightOffers)
            } else {
                val errorMessage = "No flights found for the given parameters."
                ErrorDialogFragment.newInstance(errorMessage).show(childFragmentManager, "error_dialog")
            }
        }


        val backButton = view.findViewById<ImageButton>(R.id.flightsBackButton)
        val nextButton = view.findViewById<Button>(R.id.nextButton)

        backButton.setOnClickListener {
            findNavController().navigate(R.id.action_flightResultsFragment_to_tripSearchFragment)
        }

        nextButton.setOnClickListener {
            val fi = adapter.getSelectedFlightInfo()
            if (fi != null) {
                val bundle = Bundle()
                bundle.putString("cityName", cityName)
                bundle.putString("departDate", departDate)
                bundle.putString("returnDate", returnDate)
                bundle.putString("origin", origin)
                bundle.putString("destination", destination)
                bundle.putString("budget", budget.toString())

                bundle.putString("departure.iataCode", fi.itineraries[0].segments[0].departure.iataCode)
                bundle.putString("departure.terminal", fi.itineraries[0].segments[0].departure.terminal)
                bundle.putString("departure.dateTime", fi.itineraries[0].segments[0].departure.dateTime)

                bundle.putString("arrival.iataCode", fi.itineraries[0].segments[0].arrival.iataCode)
                bundle.putString("arrival.terminal", fi.itineraries[0].segments[0].arrival.terminal ?: "N/A")
                bundle.putString("arrival.dateTime", fi.itineraries[0].segments[0].arrival.dateTime)

                bundle.putString("carrierCode", fi.itineraries[0].segments[0].carrierCode)
                bundle.putString("duration", fi.itineraries[0].segments[0].duration)

                bundle.putString("departure2.iataCode", fi.itineraries[1].segments[0].departure.iataCode)
                bundle.putString("departure2.terminal", fi.itineraries[1].segments[0].departure.terminal ?: "N/A")
                bundle.putString("departure2.dateTime", fi.itineraries[1].segments[0].departure.dateTime)

                bundle.putString("arrival2.iataCode", fi.itineraries[1].segments[0].arrival.iataCode)
                bundle.putString("arrival2.terminal", fi.itineraries[1].segments[0].arrival.terminal)
                bundle.putString("arrival2.dateTime", fi.itineraries[1].segments[0].arrival.dateTime)

                bundle.putString("carrierCode2", fi.itineraries[1].segments[0].carrierCode)
                bundle.putString("duration2", fi.itineraries[1].segments[0].duration)

                bundle.putString("price.total", fi.price.total)

                bundle.putString("latLong", latLong)

                findNavController().navigate(R.id.action_flightResultsFragment_to_resultsFragment, bundle)
            } else {
                Toast.makeText(requireContext(), "Please select a flight", Toast.LENGTH_SHORT).show()
            }
        }
        return view
    }


}