package com.example.tripplanner

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tripplanner.adapters.ItineraryAdapter
import com.example.tripplanner.viewmodels.ItineraryViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HomeScreenFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewModel: ItineraryViewModel
    private lateinit var adapter: ItineraryAdapter
    private lateinit var textViewEmpty: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home_screen, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewTrips)
        textViewEmpty = view.findViewById(R.id.textViewEmpty)
        recyclerView.layoutManager = LinearLayoutManager(context)

        //The plus icon button which allows the user to make a new trip
        view.findViewById<FloatingActionButton>(R.id.fab_add)?.setOnClickListener {
            findNavController().navigate(R.id.action_homeScreenFragment_to_tripSearchFragment)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(ItineraryViewModel::class.java)

        //Get the parameters for the clicked trip and move to trip page to display in mroe detail
        adapter = ItineraryAdapter(emptyList()) { tripId ->
            val bundle = Bundle().apply { putInt("tripId", tripId) }
            findNavController().navigate(R.id.action_homeScreenFragment_to_tripPage, bundle)
        }
        recyclerView.adapter = adapter


        viewModel.allItineraries.observe(viewLifecycleOwner) { itineraries ->
            adapter.updateData(itineraries)

            //Only show the the "No trips yet" text if there actually aren't any
            textViewEmpty.visibility = if (itineraries.isEmpty()) View.VISIBLE else View.GONE
        }
    }
}
