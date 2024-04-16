package com.example.tripplanner

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.navigation.fragment.findNavController

class TripSearch : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_trip_search, container, false)
        val buttonSearch = view.findViewById<Button>(R.id.searchButton)
        val editText = view.findViewById<EditText>(R.id.flyingTo)
        buttonSearch.setOnClickListener {
            val destination = editText.text.toString()
            val bundle = Bundle()
            bundle.putString("cityName", destination)
            findNavController().navigate(R.id.action_tripSearchFragment_to_resultsFragment, bundle)
        }
        return view
    }
}