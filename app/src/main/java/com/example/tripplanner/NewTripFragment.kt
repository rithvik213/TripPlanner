package com.example.tripplanner

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NewTripFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_new_trip, container, false)

        setupSeekBar(view)
        setupSearchButton(view)

        return view
    }

    private fun setupSeekBar(view: View) {
        val seekBar = view.findViewById<SeekBar>(R.id.priceRangeSeekBar)
        val textViewCurrentPrice = view.findViewById<TextView>(R.id.textViewCurrentPrice)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val maxPrice = 1000
                val currentPrice = progress * maxPrice / seekBar.max
                textViewCurrentPrice.text = "$$currentPrice"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    private fun setupSearchButton(view: View) {
        val buttonSearch = view.findViewById<Button>(R.id.buttonSearch)
        val editTextCityAirport = view.findViewById<EditText>(R.id.editTextCityAirport)
        val editTextDepartureDate = view.findViewById<EditText>(R.id.editTextDepartureDate)
        val textViewCurrentPrice = view.findViewById<TextView>(R.id.textViewCurrentPrice)

        buttonSearch.setOnClickListener {
            Log.d("NewTripFragment", "Search button clicked.")
            val cityAirport = editTextCityAirport.text.toString().trim()
            val userBudget = textViewCurrentPrice.text.toString().replace(Regex("[^\\d.]"), "").toDoubleOrNull()

            if (userBudget == null) {
                Log.e("NewTripFragment", "Invalid budget.")
                return@setOnClickListener
            }

            resolveCityAirportToEntityId(cityAirport) { fromEntityId ->
                if (fromEntityId != null) {
                    val departureDate = editTextDepartureDate.text.toString()
                    val departureYear = departureDate.split("/")[2]
                    val departureMonth = departureDate.split("/")[0]

                    RetrofitClient.instance.searchFlightsEverywhere(
                        fromEntityId = fromEntityId,
                        year = departureYear,
                        month = departureMonth,
                        adults = 1,
                        cabinClass = "economy",
                        children = 0,
                        currency = "USD",
                        infants = 0,
                        locale = "en-US",
                        market = "US"
                    ).enqueue(object : Callback<FlightSearchResponse> {
                        override fun onResponse(call: Call<FlightSearchResponse>, response: Response<FlightSearchResponse>) {
                            if (response.isSuccessful && response.body() != null) {
                                showDestinationsWithinBudget(response.body()!!, userBudget)
                            } else {
                                Log.e("API Response", "Error: ${response.errorBody()?.string()}")
                            }
                        }

                        override fun onFailure(call: Call<FlightSearchResponse>, t: Throwable) {
                            Log.e("API Response", "Failure: ${t.message}")
                        }
                    })
                } else {
                    Log.e("NewTripFragment", "City/Airport could not be resolved to an entity ID.")
                }
            }
        }

    }


    private fun resolveCityAirportToEntityId(cityAirport: String, completion: (String?) -> Unit) {
        RetrofitClient.instance.autocompleteLocation(query = cityAirport).enqueue(object : Callback<AutocompleteResponse> {
            override fun onResponse(call: Call<AutocompleteResponse>, response: Response<AutocompleteResponse>) {
                if (response.isSuccessful && response.body()?.data != null && response.body()?.data!!.isNotEmpty()) {
                    // Extract the 'id' from the 'presentation' object of the first matching location
                    val entityId = response.body()?.data?.firstOrNull()?.presentation?.id
                    if (entityId != null) {
                        Log.d("NewTripFragment", "Entity ID: $entityId")
                        completion(entityId)
                    } else {
                        Log.d("NewTripFragment", "Entity ID is null or not found")
                        completion(null)
                    }
                } else {
                    Log.e("NewTripFragment", "Response unsuccessful or data is empty/null")
                    completion(null)
                }
            }

            override fun onFailure(call: Call<AutocompleteResponse>, t: Throwable) {
                Log.e("NewTripFragment", "API Failure", t)
                completion(null)
            }
        })
    }


    fun showDestinationsWithinBudget(response: FlightSearchResponse, userBudget: Double) {
        val destinationsWithinBudget = response.data.everywhereDestination.results
            .mapNotNull { result ->
                result.content.flightQuotes?.cheapest?.let { cheapest ->
                    if (cheapest.rawPrice <= userBudget) {
                        "${result.content.location.airportName ?: "Unknown Airport"} " +
                                "(${result.content.location.airportCode ?: "Code"}) - " +
                                "${result.content.location.state ?: "Unknown State"}, " +
                                "${result.content.location.name} - ${cheapest.price}"
                    } else null
                }
            }

        if (destinationsWithinBudget.isNotEmpty()) {
            // Prepare the string to display in the dialog
            val destinationsString = destinationsWithinBudget.joinToString(separator = "\n")

            // Show AlertDialog with the destinations
            AlertDialog.Builder(context)
                .setTitle("Destinations Within Budget")
                .setMessage(destinationsString)
                .setPositiveButton("OK") { dialog, which -> dialog.dismiss() }
                .show()
        } else {
            // No destinations found within the budget
            AlertDialog.Builder(context)
                .setTitle("No Destinations Found")
                .setMessage("No destinations were found within your specified budget.")
                .setPositiveButton("OK") { dialog, which -> dialog.dismiss() }
                .show()
        }
    }



}
