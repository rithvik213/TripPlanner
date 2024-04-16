package com.example.tripplanner

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Results : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ExcursionAdapter

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

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.excursionRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(activity)

        val excursions = listOf(
            Excursion("Beach Volleyball", "10:00 AM"),
            Excursion("City Tour", "11:00 AM"),
        )

        adapter = ExcursionAdapter(excursions)
        recyclerView.adapter = adapter

    }
}
