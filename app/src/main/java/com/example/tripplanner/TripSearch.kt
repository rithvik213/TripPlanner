package com.example.tripplanner

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController

class TripSearch : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_trip_search, container, false)
        val buttonSearch = view.findViewById<Button>(R.id.searchButton)
        buttonSearch.setOnClickListener {
            findNavController().navigate(R.id.action_tripSearchFragment_to_resultsFragment)
        }
        return view
    }
}