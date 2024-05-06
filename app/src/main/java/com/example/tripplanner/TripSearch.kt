package com.example.tripplanner

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import java.util.Calendar
import androidx.navigation.fragment.findNavController
import com.example.tripplanner.viewmodels.SharedViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader

class TripSearch : Fragment() {

    private lateinit var viewModel: SharedViewModel
    private var departFormatted = ""
    private var returnFormatted = ""
    private var latLong = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val city = arguments?.getString("destinationCity", "Default City")
        val destinationAutoComplete = view.findViewById<EditText>(R.id.destinationEditText)
        destinationAutoComplete.setText(city)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_trip_search, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        val buttonSearch = view.findViewById<Button>(R.id.searchButton)
        val destEditText = view.findViewById<EditText>(R.id.destinationEditText)
        val originAirport = view.findViewById<AutoCompleteTextView>(R.id.airportOrigin)
        val destAirport = view.findViewById<AutoCompleteTextView>(R.id.airportDest)

        buttonSearch.setOnClickListener {
            val destLen = destAirport.text.toString().length
            val originLen = originAirport.text.toString().length
            val budget = view.findViewById<EditText>(R.id.budget).text.toString()
            if (destEditText.text.equals("") || departFormatted.equals("") || returnFormatted.equals("") || budget.equals("0") || destLen == 0 || originLen == 0){
                Toast.makeText(context, "Please fill out all fields.", Toast.LENGTH_SHORT).show()
            } else {
                //Specifies the portion of the string that corresponds to IATA codes
                val destinationIATA = destAirport.text.subSequence(destLen-3, destLen).toString()
                val originIATA = originAirport.text.subSequence(originLen-3, originLen).toString()
                val destination = destEditText.text.toString()

                val bundle = Bundle()
                bundle.putString("cityName", destination)
                bundle.putString("departDate", departFormatted)
                bundle.putString("returnDate", returnFormatted)
                bundle.putString("origin", originIATA)
                bundle.putString("destination", destinationIATA)
                bundle.putString("budget", budget)
                bundle.putString("latLong", latLong)
                findNavController().navigate(R.id.action_tripSearchFragment_to_flightResultsFragment, bundle)
            }
        }


        val budgetSeekbar: SeekBar = view.findViewById(R.id.budgetseekbar)
        val budget: EditText = view.findViewById(R.id.budget)
        budgetSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    budget.setText("$" + progress.toString())
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        val departcalendar = view.findViewById<Button>(R.id.departButton)
        val returncalendar = view.findViewById<Button>(R.id.returnButton)

        departcalendar.setOnClickListener {
            showDatePickerDialog(departcalendar)
        }

        returncalendar.setOnClickListener {
            showDatePickerDialog(returncalendar)
        }

        setupRadioButtonListeners(view)

        val autoCompleteMap = jsonFileToAirportMap()
        val autoCompleteStrings = autoCompleteMap.keys.toList()
        val adapter = ArrayAdapter(view.context, android.R.layout.simple_dropdown_item_1line, autoCompleteStrings)
        originAirport.setAdapter(adapter)
        destAirport.setAdapter(adapter)

        var lastSelectedOrigin: String? = null
        var lastSelectedDest: String? = null

        originAirport.setOnItemClickListener { _, _, position, _ ->
            lastSelectedOrigin = adapter.getItem(position).toString()
            latLong = autoCompleteMap[lastSelectedOrigin].toString()
            Log.i("originAirport", lastSelectedOrigin.toString())
        }

        destAirport.setOnItemClickListener { _, _, position, _ ->
            lastSelectedDest = adapter.getItem(position).toString()
            latLong = autoCompleteMap[lastSelectedDest].toString()
            Log.i("destAirport", lastSelectedDest.toString())
        }

        originAirport.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                originAirport.setText(lastSelectedOrigin)
            }
        }

        destAirport.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                destAirport.setText(lastSelectedDest)
            }
        }
        return view
    }

    // Allows us to save the user preferences for excursion types to be used later in generating their itinerary
    private fun setupRadioButtonListeners(view: View) {
        val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroup)
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val radioButton = view.findViewById<RadioButton>(checkedId)
            val attraction = radioButton.text.toString()
            val currentSelections = viewModel.selectedAttractions.value?.toMutableList() ?: mutableListOf()
            currentSelections.add(attraction)
            viewModel.selectedAttractions.value = currentSelections
        }
    }

    private fun showDatePickerDialog(dateButton: Button) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            val formattedDate = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear)
            dateButton.text = formattedDate
            if (dateButton.id == R.id.departButton) {
                departFormatted = String.format("%d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
            } else {
                returnFormatted = String.format("%d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                // Check if the selected return date is within 5 days from the departure date
                val departDate = Calendar.getInstance()
                val returnDate = Calendar.getInstance()
                departDate.set(departFormatted.substring(0, 4).toInt(), departFormatted.substring(5, 7).toInt() - 1, departFormatted.substring(8, 10).toInt())
                returnDate.set(selectedYear, selectedMonth, selectedDay)
                if ((returnDate.timeInMillis - departDate.timeInMillis) / (24 * 60 * 60 * 1000) > 5) {
                    Toast.makeText(context, "Return date must be within 5 days from departure date.", Toast.LENGTH_LONG).show()
                    dateButton.text = "Select Date"
                    returnFormatted = ""
                }
            }
        }, year, month, day)

        if (dateButton.id == R.id.departButton) {
            datePickerDialog.datePicker.minDate = calendar.timeInMillis
        } else {
            val minReturnCalendar = Calendar.getInstance()
            minReturnCalendar.set(departFormatted.substring(0, 4).toInt(), departFormatted.substring(5, 7).toInt() - 1, departFormatted.substring(8, 10).toInt())
            minReturnCalendar.add(Calendar.DAY_OF_MONTH, 1)  // Minimum return date is the day after departure
            datePickerDialog.datePicker.minDate = minReturnCalendar.timeInMillis

            val maxReturnCalendar = Calendar.getInstance()
            maxReturnCalendar.set(departFormatted.substring(0, 4).toInt(), departFormatted.substring(5, 7).toInt() - 1, departFormatted.substring(8, 10).toInt())
            maxReturnCalendar.add(Calendar.DAY_OF_MONTH, 5)  // Maximum return date is 5 days after departure
            datePickerDialog.datePicker.maxDate = maxReturnCalendar.timeInMillis
        }

        datePickerDialog.show()
    }


    private fun jsonFileToAirportMap(): HashMap<String, String> {
        val jsonFile = context?.assets?.open("airports.json")
        val reader = BufferedReader(InputStreamReader(jsonFile))
        val stringBuilder = StringBuilder()
        reader.useLines { lines -> lines.forEach { stringBuilder.append(it) } }
        val jsonString = stringBuilder.toString()

        val gson = Gson()
        val listType = object : TypeToken<List<AirportEntry>>() {}.type
        val airportEntries: List<AirportEntry> = gson.fromJson(jsonString, listType)

        val map = HashMap<String, String>()
        for (entry in airportEntries) {
            map[entry.name] = entry.latLong
        }
        return map
    }
}
data class AirportEntry(val name: String, val latLong: String)

