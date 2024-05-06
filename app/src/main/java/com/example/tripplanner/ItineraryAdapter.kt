package com.example.tripplanner

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ItineraryAdapter(private var itineraries: List<Itinerary>, private val onItineraryClicked: (Int) -> Unit) :
    RecyclerView.Adapter<ItineraryAdapter.ItineraryViewHolder>() {

    class ItineraryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.tripdestination)
        val imageViewTrip: ImageView = view.findViewById(R.id.image_trip)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItineraryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.tripcards, parent, false)
        return ItineraryViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItineraryViewHolder, position: Int) {
        val itinerary = itineraries[position]
        holder.textView.text = itinerary.cityName
        Glide.with(holder.itemView.context).load(itinerary.imageUrl).into(holder.imageViewTrip)
        holder.itemView.setOnClickListener {
            onItineraryClicked(itinerary.id)
        }
    }

    override fun getItemCount(): Int = itineraries.size

    fun updateData(newItineraries: List<Itinerary>) {
        itineraries = newItineraries
        notifyDataSetChanged()
    }
}