package com.example.tripplanner.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tripplanner.data.Attraction
import com.example.tripplanner.R
import com.example.tripplanner.apis.tripadvisor.TripAdvisorManager

/* Adapter that allows us to display nearby attractions for users who have
* allowed location permissions on our discover page and puts text below
* fetched image and sets each element of RecyclerView as button that can
* be clicked to get a further description
 */
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




