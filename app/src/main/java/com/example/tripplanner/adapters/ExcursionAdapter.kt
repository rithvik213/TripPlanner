package com.example.tripplanner.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tripplanner.data.Excursion
import com.example.tripplanner.R

class ExcursionAdapter(private var excursionList: MutableList<Excursion>) :
    RecyclerView.Adapter<ExcursionAdapter.ExcursionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExcursionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_excursion, parent, false)
        return ExcursionViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExcursionViewHolder, position: Int) {
        val excursion = excursionList[position]
        holder.titleTextView.text = excursion.name
        holder.subtitleTextView.text = excursion.time
    }

    override fun getItemCount() = excursionList.size

    fun updateExcursions(newExcursions: MutableList<Excursion>) {
        excursionList.clear()
        excursionList.addAll(newExcursions)
        notifyDataSetChanged()
    }

    class ExcursionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.textViewTitle)
        val subtitleTextView: TextView = view.findViewById(R.id.textViewSubtitle)
    }
}

