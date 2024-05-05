package com.example.tripplanner

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tripplanner.apis.amadeus.data.FlightOffer
import com.example.tripplanner.apis.amadeus.fetchFlightOffers
import kotlinx.coroutines.launch


class FlightResults : Fragment() {
    private lateinit var cityName: String
    private lateinit var departDate: String
    private var budget = 0
    private lateinit var origin: String
    private lateinit var destination: String
    private lateinit var returnDate: String

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

        return view
    }


}