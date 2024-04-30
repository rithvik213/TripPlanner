package com.example.tripplanner

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;


import com.bumptech.glide.Glide


class DestinationsPage : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_destinations_page, container, false)
        val destinationTitle = arguments?.getString("destinationTitle", "Default Title")
        val destinationDescription = arguments?.getString("destinationDescription")
        var destinationImageURL = arguments?.getString("destinationImageURL")

        view.findViewById<TextView>(R.id.destinationdescription).text = destinationDescription
        view.findViewById<TextView>(R.id.destinationname).text = destinationTitle
        val imageView = view.findViewById<ImageView>(R.id.destinationimage)
        if (destinationImageURL != null) {
            Glide.with(this)
                .load(destinationImageURL)
                .into(imageView)
        }
        return view
    }


}