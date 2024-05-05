package com.example.tripplanner.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import com.bumptech.glide.Glide
import com.example.tripplanner.data.Destination
import com.example.tripplanner.R


class PopularDestinationsAdapter(private val items: List<Destination>, private val onDestinationClicked: (Destination) -> Unit) : RecyclerView.Adapter<PopularDestinationsAdapter.ViewHolder>() {

    class ViewHolder(view: View, onClick: (Int) -> Unit) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.populardestinationimageView)
        val titleView: TextView = view.findViewById(R.id.populardestinationTitle)

        init {
            view.setOnClickListener {
                onClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.populardestinationscards, parent, false)
        return ViewHolder(view) { position ->
            onDestinationClicked(items[position])
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.titleView.text = item.title
        if (item.imageUrl != null) {
            Glide.with(holder.itemView.context)
                .load(item.imageUrl)
                .into(holder.imageView)
        }
    }


    override fun getItemCount() = items.size
}
