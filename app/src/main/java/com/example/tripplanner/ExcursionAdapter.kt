package com.example.tripplanner

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ExcursionAdapter(private val excursionList: List<Excursion>) :
    RecyclerView.Adapter<ExcursionAdapter.ExcursionViewHolder>() {

    class ExcursionViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExcursionViewHolder {
        val textView = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false) as TextView
        return ExcursionViewHolder(textView)
    }

    override fun onBindViewHolder(holder: ExcursionViewHolder, position: Int) {
        val excursion = excursionList[position]
        holder.textView.text = "${excursion.name} - ${excursion.time}"
    }

    override fun getItemCount() = excursionList.size
}
