package com.example.tripplanner

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ExcursionAdapter(private val excursionList: List<Excursion>) :
    RecyclerView.Adapter<ExcursionAdapter.ExcursionViewHolder>() {

    class ExcursionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(android.R.id.text1)
        val subtitleTextView: TextView = view.findViewById(android.R.id.text2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExcursionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return ExcursionViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExcursionViewHolder, position: Int) {
        val excursion = excursionList[position]
        holder.titleTextView.text = excursion.name
        holder.subtitleTextView.text = excursion.time
    }

    override fun getItemCount() = excursionList.size
}

