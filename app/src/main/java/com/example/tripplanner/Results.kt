package com.example.tripplanner

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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
        return inflater.inflate(R.id.excursionRecyclerView, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.excursionRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(activity)

        // Sample data for excursions
        val excursions = listOf(
            Excursion("Beach Volleyball", "10:00 AM"),
            Excursion("City Tour", "11:00 AM"),
            // Add more excursions here
        )

        adapter = ExcursionAdapter(excursions)
        recyclerView.adapter = adapter

    }
}
