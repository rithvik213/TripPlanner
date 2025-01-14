package com.example.tripplanner.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tripplanner.data.DayItinerary
import com.example.tripplanner.R


/* Adapter class that allows for itineraries of multiple days to exist as
scrollable element
*/

class ItineraryPagerAdapter(private val daysItinerary: List<DayItinerary>, private val fragment: Fragment)
    : RecyclerView.Adapter<ItineraryPagerAdapter.ItineraryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItineraryViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.day_itinerary_layout, parent, false)
        return ItineraryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItineraryViewHolder, position: Int) {
        val dayItinerary = daysItinerary[position]
        holder.bind(dayItinerary)
    }

    override fun getItemCount(): Int = daysItinerary.size

    inner class ItineraryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val recyclerView: RecyclerView = itemView.findViewById(R.id.dayExcursionRecyclerView)

        fun bind(dayItinerary: DayItinerary) {
            recyclerView.layoutManager = LinearLayoutManager(fragment.context)
            recyclerView.adapter = ExcursionAdapter(dayItinerary.excursions.toMutableList())
        }
    }
}
