package com.example.tripplanner

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class NearbyAttractionsAdapter(var items: List<TripAdvisorManager.AttractionDetail>, private val onAttractionClick: (Int) -> Unit) :
    RecyclerView.Adapter<NearbyAttractionsAdapter.ViewHolder>() {

    class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val titleView: TextView = view.findViewById(R.id.nearbyattractionTitle)
        val imageView: ImageView = view.findViewById(R.id.nearbyattractionimageView)

        fun bind(attractionDetail: TripAdvisorManager.AttractionDetail, clickListener: (Int) -> Unit) {
            titleView.text = attractionDetail.name
            if (attractionDetail.imageUrl != null) {
                Glide.with(view.context)
                    .load(attractionDetail.imageUrl)
                    .into(imageView)
            }
            view.setOnClickListener { clickListener(adapterPosition) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.nearbyattractionscards, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, onAttractionClick)
    }

    override fun getItemCount() = items.size


    fun updateData(newItems: List<TripAdvisorManager.AttractionDetail>) {
        items = newItems
        notifyDataSetChanged()
    }

    interface OnAttractionClickListener {
        fun onAttractionClick(attraction: Attraction)
    }
}




