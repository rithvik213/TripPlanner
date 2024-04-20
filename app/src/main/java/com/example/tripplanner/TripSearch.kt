package com.example.tripplanner

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.ImageButton
import java.util.Calendar
import androidx.navigation.fragment.findNavController

class TripSearch : Fragment() {
    private var departFormatted = "2025-01-01"
    private var returnFormatted = "2025-01-01"

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_trip_search, container, false)

        val buttonSearch = view.findViewById<Button>(R.id.searchButton)
        val editText = view.findViewById<EditText>(R.id.destinationAutoComplete)
        val departdate: EditText = view.findViewById(R.id.departdate)
        val returndate: EditText = view.findViewById(R.id.returndate)

        buttonSearch.setOnClickListener {
            val destination = editText.text.toString()
            val origin = view.findViewById<EditText>(R.id.originAutoComplete).text.toString()
            val budget = view.findViewById<EditText>(R.id.budget).text.toString()

            val bundle = Bundle()
            bundle.putString("cityName", destination)
            bundle.putString("departDate", departFormatted)
            bundle.putString("returnDate", returnFormatted)
            bundle.putString("origin", origin)
            bundle.putString("budget", budget)
            findNavController().navigate(R.id.action_tripSearchFragment_to_resultsFragment, bundle)
        }


        val budgetSeekbar: SeekBar = view.findViewById(R.id.budgetseekbar)
        val budget: EditText = view.findViewById(R.id.budget)
        budgetSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    budget.setText("$" + progress.toString())
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        val departcalendar = view.findViewById<ImageButton>(R.id.departcalendar)
        val returncalendar = view.findViewById<ImageButton>(R.id.returncalendar)

        departcalendar.setOnClickListener {
            showDatePickerDialog(departdate, departFormatted)
        }

        returncalendar.setOnClickListener {
            showDatePickerDialog(returndate, returnFormatted)
        }
        return view
    }

    private fun showDatePickerDialog(dateEditText: EditText, otherFormat: String) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            val formattedDate = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear)
            dateEditText.setText(formattedDate)
            if(dateEditText.id == R.id.departdate)
                departFormatted = String.format("%d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
            else
                returnFormatted = String.format("%d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
        }, year, month, day)


        datePickerDialog.show()
    }

}
